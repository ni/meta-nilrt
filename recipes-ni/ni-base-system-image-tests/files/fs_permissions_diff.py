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
import fs_permissions_shared
import re

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

class OsVersion:
    def __init__(self, os_version_full=None, os_version_codename=None):
        if os_version_full is not None:
            self.full = os_version_full
            major_minor_version = re.match(r'(\d+.\d+).*', self.full)
            self.major_minor = major_minor_version.group(1)
            self.codename = os_version_codename
        else:
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
            'build': self.build
        }

fs_manifest_format = '%p\t%M\t%u\t%g\t%l'
fs_manifest_columns = ['path', 'mode', 'user', 'group', 'link_target']
def get_fs_manifest():
    search_dirs = ['/bin', '/boot', '/etc', '/lib', '/lib64', '/sbin', '/usr', '/var']
    omit_dirs = ['/etc/natinst/niskyline/Data/Assets/Cache', '/lib/modules', '/var/cache', '/var/run', '/var/tmp', '/var/volatile']

    omit_expr = []
    for d in omit_dirs:
        omit_expr += ['-path', d, '-o']
    omit_expr.pop()

    fs_manifest = fs_permissions_shared.run_cmd(
        ['find']
        + search_dirs
        + ['-printf', fs_manifest_format + '\n']
        + ['('] + omit_expr + [')', '-prune']
    )
    return '\n'.join(sorted(fs_manifest.splitlines()))

def log_version_info(logger, label, codename, full_version, date, db_id):
    logger.log(f'INFO: {label} = {codename} {full_version} from {date} with _id {db_id}')

def upload_manifest(db, fs_manifest, os_version, logger):
    data = {}
    data['os_version_codename'] = os_version.codename
    data['os_version_full'] = os_version.full
    data['os_version'] = os_version.version_dict
    data['date'] = str(datetime.datetime.now())

    header = ''
    for key, val in data.items():
        header = header + '# {}: {}\n'.format(key, val)

    data['fs_permissions'] = header + fs_manifest

    record = db.insert(data)
    return data['date'], record.inserted_id

def strip_headers(fs_manifest):
    return ''.join(line + '\n' for line in fs_manifest.splitlines() if not line.startswith('#'))

def get_previous_fs_manifest_as_current(db, previous_date, logger):
    query = {}
    query['date'] = previous_date
    count = db.count_documents(query)
    logger.log('!!!!!OVERRIDING CURRENT MANIFEST!!!!!\n'.format(count))
    logger.log('Found {} records\n'.format(count))
    if count == 1:
        results = db.find(query).limit(1)
        result = next(results)
        logger.log('INFO: Found fs_manifest record with these details')
        return strip_headers(result['fs_permissions']), result['os_version_full'], result['os_version_codename'], result['date'], result['_id']
    elif count > 1:
        logger.log('INFO: Found multiple fs manifest records with the same date: {}'.format(previous_date))
        exit(1)
    else:
        logger.log('INFO: Could not find fs manifest record with date: {}'.format(previous_date))
        exit(1)

def get_old_fs_manifests(db, logger, os_version, basis_override):
    def run_query(label, query):
        nonlocal db
        nonlocal logger

        sort_order = [('os_version', pymongo.DESCENDING), ('date', pymongo.DESCENDING)]

        if db.count_documents(query):
            results = db.find(query).sort(sort_order).limit(1)
        else:
            # If there are no results, there must not be a run from the current os codename.
            # Remove that requirement.
            if 'os_version_codename' in query:
                del query['os_version_codename']
                logger.log('INFO: No prior fs permissions from this OS codename found. Relaxing that constraint.')

            if db.count_documents(query):
                results = db.find(query).sort(sort_order).limit(1)
            else:
                logger.log('INFO: No suitable previous fs permissions found')
                return '', '<none>', '<none>'

        result = next(results)

        log_version_info(
            logger,
            label,
            result['os_version_codename'],
            result['os_version_full'],
            result['date'],
            result['_id']
        )

        return strip_headers(result['fs_permissions']), result['os_version_full'], result['date']

    query_version_build = lambda build: {
        'os_version_codename': os_version.codename,
        'os_version': {
            '$lt': {
                'major': os_version.major,
                'minor': os_version.minor,
                'patch': os_version.patch,
                'build': build
            }
        }
    }

    query_date_build = lambda date: {
        'date': date
    }

    if basis_override is not None:
        logger.log(f'INFO: Basis manifest was specified to have date: {basis_override}')
        basis_manifest, basis_version_full, basis_date = run_query('basis', query_date_build(basis_override))
    else:
        # If not specified, the "basis" manifest is the most recent version with a lesser MAJOR.MINOR.PATCH than the current manifest.
        # In general, it should be the last final build of the previous MAJOR.MINOR.PATCH version.
        basis_manifest, basis_version_full, basis_date = run_query('basis', query_version_build(0))

    # If the current version and basis version are the same (most likely scenario is if basis version is pinned to the most recent build available),
    # then just set recent to the same version as well. The normal mechanism of querying for the most recent MAJOR.MINOR.PATCH.BUILD less than the
    # current version is going to result in a recent version that is *older* than the basis version, which doesn't make any sense.
    if os_version.full == basis_version_full:
        recent_manifest, recent_version_full, unused = run_query('recent', query_date_build(basis_date))
    else:
        # If not specified, the "recent" manifest is the most recent version less than the current manifest.
        # In general, it will be the previous build with the same MAJOR.MINOR.PATCH. Or if current is the first build of a new MAJOR.MINOR.PATCH,
        # then it will be the same as the basis version.
        recent_manifest, recent_version_full, unused = run_query('recent', query_version_build(os_version.build))

    # Keep this log in first line to help streak indexer group results. Overall hash will be populated at end.
    logger.prefix_log(f'INFO: fs_permissions_diff: current against {basis_version_full} ')
    # Keep this log in second line to avoid using it for grouping, but keep it for tracking.
    # Individual hashes to be populated at end.
    logger.prefix_log(f'INFO:   and {recent_version_full} ')

    return basis_manifest, recent_manifest

def strip_versions_from_path(path):
    # Strip all things that look like version numbers to simplify the diff
    # 
    # The below order matters, since the last sub will prevent previous ones from working correctly

    # Strip all versions for salt (2 forms of this)
    stripped_path = re.sub(r"salt-\d+\.\d+_\d+_g[0-9a-f]+", "salt-SALTVERSION", path)
    stripped_path = re.sub(r"salt-\d+\.\d+\+\d+\.g[0-9a-f]+", "salt-SALTVERSION", stripped_path)
    # Strip everything that looks like an RT kernel version (so module and kernel version changes are ignored)
    stripped_path = re.sub(r"\d+\.\d+\.\d+-rt\d+", "VERSION.VERSION.VERSION-rtVERSION", stripped_path)
    # Strip everything that looks like 1.2.3
    stripped_path = re.sub(r"\d+\.\d+\.\d+", "VERSION.VERSION.VERSION", stripped_path)
    # Strip everything that looks like 1.2
    stripped_path = re.sub(r"\d+\.\d+", "VERSION.VERSION", stripped_path)

    return stripped_path

def prepare_manifest_for_diff(manifest):
    reader = csv.DictReader(manifest.splitlines(), fieldnames=fs_manifest_columns, delimiter='\t')
    result = {}
    for row in reader:
        stripped_path = strip_versions_from_path(row['path'])
        result[stripped_path] = row
    return result

class IntermediateDiff:
    def __init__(self, old, mid, new):
        # ignores symlink targets
        is_same = lambda old, new: (old['mode'] == new['mode']
                                    and old['user'] == new['user']
                                    and old['group'] == new['group'])
        changes = lambda old, new: {path for path in old.keys() & new.keys() if not is_same(old[path], new[path])}
        adds = lambda old, new: new.keys() - old.keys()
        diff = lambda old, new: {
            'changed': changes(old, new),
            'added': adds(old, new),
            'removed': adds(new, old)
        }
        self.unseen = diff(mid, new)
        self.net = diff(old, new)
        self.seen = diff(old, mid)

        self.all_unseen = self.unseen['changed'] | self.unseen['added'] | self.unseen['removed']
        self.all_net = self.net['changed'] | self.net['added'] | self.net['removed']
        self.all_seen = self.seen['changed'] | self.seen['added'] | self.seen['removed']
        self.all_different = (list(sorted(self.all_unseen))
                              + list(sorted(self.all_net - self.all_unseen))
                              + list(sorted(self.all_seen - self.all_net - self.all_unseen)))

    def lookup_path(self, path):
        result = {}
        relations = ['unseen', 'net', 'seen']
        operations = ['changed', 'added', 'removed']
        for rel in relations:
            if path in self.__dict__['all_' + rel]:
                for op in operations:
                    if path in self.__dict__[rel][op]:
                        result[rel] = op
                        if op in result:
                            result[op] |= {rel}
                        else:
                            result[op] = {rel}
        return result

    def differences(self):
        for path in self.all_different:
            difference = self.lookup_path(path)
            difference['path'] = path
            yield difference

def diff_manifests(current_manifest, basis_manifest, recent_manifest, logger):
    hash_and_prep = lambda manifest: (hashlib.md5(manifest.encode('utf-8')).hexdigest(),
                                      prepare_manifest_for_diff(manifest))
    current_hash, current_manifest = hash_and_prep(current_manifest)
    basis_hash, basis_manifest = hash_and_prep(basis_manifest)
    recent_hash, recent_manifest = hash_and_prep(recent_manifest)

    # Reduce to just one hash (recent is irrelevant to test failures, and causes duplicates)
    overall_hash = hashlib.md5(f'{basis_hash}\n{current_hash}\n'.encode('utf-8')).hexdigest()
    # Add hash to first line of log to allow review queue to distinguish runs,
    logger.prefix_logs[0] += overall_hash
    # and add each hash on second line for tracking
    logger.prefix_logs[1] += f'(basis={basis_hash}, recent={recent_hash}, current={current_hash})'

    diff = IntermediateDiff(basis_manifest, recent_manifest, current_manifest)

    detail_string = lambda name, manifest: lambda path: (f'{name} '
                                                         + ', '.join([
                                                             f"mode = {manifest[path]['mode']}",
                                                             f"user = {manifest[path]['user']}",
                                                             f"group = {manifest[path]['group']}"]))
    basis_detail =   detail_string('basis  ', basis_manifest)
    recent_detail =  detail_string('recent ', recent_manifest)
    current_detail = detail_string('current', current_manifest)

    op_markers = {'changed': 'c', 'added': '+', 'removed': '-'}

    logger.log('\n')
    logger.log('###### Differences since last test run: current vs recent ######')
    logger.log('(Note that these are not necessarily responsible for test failure; see full diff below)')
    logger.log('LEGEND:')
    logger.log('c: current changed relative to recent')
    logger.log('+: current has a path that recent does not')
    logger.log('-: recent has a path that current does not')
    logger.log('###### begin last-run diff ######')

    for path in sorted(diff.all_unseen):
        difference = diff.lookup_path(path)
        msg_lines = [f"{op_markers[difference['unseen']]} {path}"]
        if 'changed' == difference['unseen']:
            msg_lines += [f'{current_detail(path)}', f'{recent_detail(path)}']
        elif 'added' == difference['unseen']:
            msg_lines += [f'{current_detail(path)}']
        elif 'removed' == difference['unseen']:
            msg_lines += [f'{recent_detail(path)}']
        logger.log('\n  '.join(msg_lines))

    logger.log('###### end last-run diff ######')

    logger.log('\n')
    logger.log('###### Full three-way diff: current vs basis, recent vs basis, current vs recent ######')
    logger.log('(Note that only the first column is responsible for test failure)')
    logger.log('LEGEND:')
    logger.log('c: X changed relative to Y')
    logger.log('+: X has a path that Y does not')
    logger.log('-: Y has a path that X does not')
    logger.log('*   X=current Y=basis  (new issues since the last run for previous MAJOR.MINOR.PATCH)') # net
    logger.log(' *  X=recent  Y=basis  (issues seen in the last run)') # seen
    logger.log('  * X=current Y=recent (new issues since the last run, probably for this MAJOR.MINOR.PATCH)') # unseen
    logger.log('###### begin full diff ######')

    any_differences = False
    for difference in diff.differences():
        path = difference['path']
        msg_lines = ['']
        show_basis = False
        show_recent = False
        show_current = False
        if 'net' in difference:
            any_differences = True
            msg_lines[0] += op_markers[difference['net']]
            show_basis = True
            show_current = True
        else:
            msg_lines[0] += ' '
        if 'seen' in difference:
            msg_lines[0] += op_markers[difference['seen']]
            show_basis = True
            show_recent = True
        else:
            msg_lines[0] += ' '
        if 'unseen' in difference:
            msg_lines[0] += op_markers[difference['unseen']]
            show_recent = True
            show_current = True
        else:
            msg_lines[0] += ' '
        msg_lines[0] += f' {path}'
        if show_current and path in current_manifest:
            msg_lines.append(current_detail(path))
        if show_basis and path in basis_manifest:
            msg_lines.append(basis_detail(path))
        if show_recent and path in recent_manifest:
            msg_lines.append(recent_detail(path))
        logger.log('\n    '.join(msg_lines))

    logger.log('###### end full diff ######')

    return not any_differences

def prepare_system_for_manifest():
    # logrotate may not have run yet, so run it now to generate
    # the logrotate.status
    subprocess.run(["/usr/sbin/logrotate", "/etc/logrotate.conf"])

def parse_args():
    parser = argparse.ArgumentParser(description='diff fs permissions with previous')
    parser.add_argument('--server', required=True, help='Mongo server hostname')
    parser.add_argument('--user', required=True, help='Mongo server username')
    parser.add_argument('--password', required=True, help='Mongo server password')
    parser.add_argument('--current_log_db_date', metavar="<date>",
                        help='Use this flag to supply a date string that will be used to locate a filesystem manifest in the database. That log will be used as the current filesystem manifest. '\
                             'Should be of the format "2023-03-30 15:32:43.203476". Date strings for previous filesystem manifest can be found in the output of previous runs of this '\
                             'test or can be extracted from the Mongo database using a viewer tool like Compass. This flag also skips the upload of the filesystem manifest record to the '\
                             'database, so the --skip_upload flag is not needed. This flag is useful for debugging issues with this test.')
    parser.add_argument('--basis_log_db_date', metavar="<date>",
                        help='Use this flag to supply a date string that will be used to locate a filesystem manifest in the database. That log will be used as the basis filesystem manifest. '\
                             'Should be of the format "2023-03-30 15:32:43.203476". Date strings for previous filesystem manifest can be found in the output of previous runs of this '\
                             'test or can be extracted from the Mongo database using a viewer tool like Compass. This flag is useful for resetting the comparison baseline for the test.')
    parser.add_argument('--skip_upload', help='Skip upload of fs manifest record to database. Useful when debugging.', action="store_true")

    return parser.parse_args()

def main():
    logger = fs_permissions_shared.Logger()
    args = parse_args()
    db = DB(args.server, args.user, args.password)

    if args.current_log_db_date:
        fs_manifest, os_version_full, os_version_codename, db_date, db_id = get_previous_fs_manifest_as_current(db, args.current_log_db_date, logger)
        os_version = OsVersion(os_version_full, os_version_codename)
    else:
        prepare_system_for_manifest()
        fs_manifest = get_fs_manifest()
        os_version = OsVersion()
        db_date = "<not uploaded to database>"
        db_id = "<not uploaded to database>"

    basis_fs_manifest, recent_fs_manifest = get_old_fs_manifests(db, logger, os_version, args.basis_log_db_date)

    if not args.current_log_db_date and not args.skip_upload:
        db_date, db_id = upload_manifest(db, fs_manifest, os_version, logger)

    log_version_info(logger, 'current', os_version.codename, os_version.full, db_date, db_id)

    result = diff_manifests(fs_manifest, basis_fs_manifest, recent_fs_manifest, logger)

    logger.report()
    if result:
        sys.exit(os.EX_OK)
    else:
        sys.exit(os.EX_SOFTWARE)

if __name__ == '__main__':
    main()

