import argparse
import csv
import datetime
import hashlib
import mmap
import os
import pymongo
import re
import subprocess
import sys

class DB:
    mongo_db_name = 'rtos'
    mongo_db_collection_name = 'fs_permissions'

    def __init__(self, server, user, password):
        mongo_client = pymongo.MongoClient("mongodb://{user}:{pwd}@{server}".format(
            user=user, pwd=password, server=server
            ),
            w=1)
        db = mongo_client[self.mongo_db_name]
        self.fs_permissions_collection = db[self.mongo_db_collection_name]

    def insert(self, data):
        return self.fs_permissions_collection.insert_one(data)

    def find(self, query):
        return self.fs_permissions_collection.find(query)

    def count_documents(self, query):
        return self.fs_permissions_collection.count_documents(query)

class Logger:
    logs = []
    def log(self, log):
        self.logs.append(log)

    def first_log(self, log):
        self.logs.insert(0, log)

    def report(self):
        for log in self.logs:
            print(log)

def run_cmd(cmd):
    output = subprocess.check_output(cmd)
    return output.decode('utf-8')

class OsVersion:
    phase_number = { 'd': 0, 'a': 1, 'b': 2, 'f': 3 }

    def __init__(self):
        with open('/etc/os-release', 'rb') as file, mmap.mmap(file.fileno(), 0, access=mmap.ACCESS_READ) as mfile:
            build_id = re.search(br'BUILD_ID=\"((\d+\.\d+).*)\"', mfile)
            self.full = build_id.group(1).decode()
            self.major_minor = build_id.group(2).decode()
            codename = re.search(br'VERSION_CODENAME=\"(.*)\"', mfile)
            self.codename = codename.group(1).decode()

        decomposed_version = re.match(r'(\d+).(\d+).(\d+)([dabf])(\d+)', self.full)
        self.major = int(decomposed_version.group(1))
        self.minor = int(decomposed_version.group(2))
        self.patch = int(decomposed_version.group(3))
        self.phase = decomposed_version.group(4)
        self.build = int(decomposed_version.group(5))
        self.version_dict = {
            'major': self.major,
            'minor': self.minor,
            'patch': self.patch,
            'phase': self.phase_number[self.phase],
            'build': self.build
        }

fs_manifest_format = '%p\t%M\t%u\t%g\t%l'
fs_manifest_columns = ['path', 'mode', 'user', 'group', 'link_target']
def get_fs_manifest():
    search_dirs = ['/bin', '/boot', '/etc', '/lib', '/lib64', '/sbin', '/usr', '/var']
    omit_dirs = ['/lib/modules', '/var/cache', '/var/run', '/var/tmp', '/var/volatile']

    omit_expr = []
    for d in omit_dirs:
        omit_expr += ['-path', d, '-o']
    omit_expr.pop()

    fs_manifest = run_cmd(
        ['find']
        + search_dirs
        + ['-printf', fs_manifest_format + '\n']
        + ['('] + omit_expr + [')', '-prune']
    )
    return fs_manifest

def upload_manifest(db, fs_manifest, logger):
    data = {}
    os_version = OsVersion()
    data['os_version_codename'] = os_version.codename
    data['os_version_full'] = os_version.full
    data['os_version_major_minor'] = os_version.major_minor
    data['os_version'] = os_version.version_dict
    data['date'] = str(datetime.datetime.now())

    header = ''
    for key, val in data.items():
        header = header + '# {}: {}\n'.format(key, val)

    data['fs_permissions'] = header + fs_manifest

    record = db.insert(data)
    logger.log('INFO: Uploaded fs permissions of OS {} from {} with _id {}'.format(data['os_version_full'], data['date'], record.inserted_id))

def strip_headers(fs_manifest):
    return ''.join(line + '\n' for line in fs_manifest.splitlines() if not line.startswith('#'))

def get_old_fs_manifest(db, logger):
    os_version = OsVersion()
    query = {
        'os_version_codename': os_version.codename,
        'os_version_major_minor': os_version.major_minor,
        'os_version': {
            '$lt': {
                'major': os_version.major,
                'minor': os_version.minor,
                'patch': os_version.patch,
                # $lt considers booleans to be greater than all numbers
                'phase': False,
                'build': False
            }
        },
        'os_version.phase': OsVersion.phase_number['f']
    }

    if db.count_documents(query):
        results = db.find(query).sort('date', pymongo.DESCENDING).limit(1)
    else:
        # If there are no results, there may not be a run from the current os version.
        # Remove that requirement.
        del query['os_version_major_minor']
        logger.log('INFO: No prior fs permissions from this OS Version found. Using previous version.')

        if db.count_documents(query):
            results = db.find(query).sort('date', pymongo.DESCENDING).limit(1)
        else:
            # Keep this log in first line to help streak indexer group results. Hashes will be populated at end.
            logger.first_log('INFO: fs_permissions_diff: {} against <empty> '.format(os_version.full))

            logger.log('INFO: No suitable previous fs permissions found')
            return ''

    result = next(results)

    # Keep this log in first line to help streak indexer group results. Hashes will be populated at end.
    logger.first_log('INFO: fs_permissions_diff: {} against older log of {} '.format(os_version.full, result['os_version_full']))

    logger.log('INFO: Using previous fs permissions of OS {} from {} with _id {}'.format(result['os_version_full'], result['date'], result['_id']))
    fs_manifest = result['fs_permissions']
    return strip_headers(fs_manifest)

def prepare_manifest_for_diff(manifest):
    reader = csv.DictReader(manifest.splitlines(), fieldnames=fs_manifest_columns, delimiter='\t')
    return {row['path']: row for row in reader}

def diff_manifests(current_manifest, old_manifest, logger):
    current_hash = hashlib.md5(current_manifest.encode('utf-8')).hexdigest()
    current_manifest = prepare_manifest_for_diff(current_manifest)
    old_hash = hashlib.md5(old_manifest.encode('utf-8')).hexdigest()
    old_manifest = prepare_manifest_for_diff(old_manifest)

    removed_paths = old_manifest.keys() - current_manifest.keys()
    added_paths = current_manifest.keys() - old_manifest.keys()
    common_paths = old_manifest.keys() & current_manifest.keys()

    # ignores symlink targets
    is_same = lambda old, new: (old['mode'] == new['mode']
                                and old['user'] == new['user']
                                and old['group'] == new['group'])
    different_paths = set(path for path in common_paths if not is_same(old_manifest[path], current_manifest[path]))

    detail_string = lambda entry: f"mode = {entry['mode']}, user = {entry['user']}, group = {entry['group']}"

    any_differences = False

    if 0 != len(removed_paths):
        logger.log('INFO: Starting removed path list')
        for path in removed_paths:
            logger.log(f'{path}: {detail_string(old_manifest[path])}')
        logger.log('INFO: End of removed path list')
        any_differences = any_differences or True
    else:
        logger.log('INFO: No paths removed')

    if 0 != len(added_paths):
        logger.log('INFO: Starting added path list')
        for path in added_paths:
            logger.log(f'{path}: {detail_string(current_manifest[path])}')
        logger.log('INFO: End of added path list')
        any_differences = any_differences or True
    else:
        logger.log('INFO: No paths added')

    # Add hashes to first line of log to allow review queue to distinguish runs
    logger.logs[0] += f'(old={old_hash}, new={current_hash})'

    if 0 != len(different_paths):
        logger.log('INFO: Starting diff')
        for path in different_paths:
            logger.log('\n'.join([path,
                                  f'\told: {detail_string(old_manifest[path])}',
                                  f'\tnew: {detail_string(current_manifest[path])}']))
        logger.log('INFO: End of diff')
        any_differences = any_differences or True
    else:
        logger.log('INFO: Empty diff')

    return not any_differences

def parse_args():
    parser = argparse.ArgumentParser(description='diff fs permissions with previous')
    parser.add_argument('--server', required=True, help='Mongo server hostname')
    parser.add_argument('--user', required=True, help='Mongo server username')
    parser.add_argument('--password', required=True, help='Mongo server password')

    return parser.parse_args()

logger = Logger()
args = parse_args()
db = DB(args.server, args.user, args.password)
old_fs_manifest = get_old_fs_manifest(db, logger)

fs_manifest = get_fs_manifest()
upload_manifest(db, fs_manifest, logger)

result = diff_manifests(fs_manifest, old_fs_manifest, logger)

logger.report()

if result:
    sys.exit(os.EX_OK)
else:
    sys.exit(os.EX_SOFTWARE)
