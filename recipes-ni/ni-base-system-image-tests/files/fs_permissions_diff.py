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

def upload_manifest(db, fs_manifest, logger):
    data = {}
    os_version = OsVersion()
    data['os_version_codename'] = os_version.codename
    data['os_version_full'] = os_version.full
    data['os_version'] = os_version.version_dict
    data['date'] = str(datetime.datetime.now())

    header = ''
    for key, val in data.items():
        header = header + '# {}: {}\n'.format(key, val)

    data['fs_permissions'] = header + fs_manifest

    record = db.insert(data)

    log_version_info(
        logger,
        'current',
        data['os_version_codename'],
        data['os_version_full'],
        data['date'],
        record.inserted_id
    )

def strip_headers(fs_manifest):
    return ''.join(line + '\n' for line in fs_manifest.splitlines() if not line.startswith('#'))

def get_old_fs_manifests(db, logger):
    def run_query(label, query):
        nonlocal db
        nonlocal logger

        sort_order = [('os_version', pymongo.DESCENDING), ('date', pymongo.DESCENDING)]

        if db.count_documents(query):
            results = db.find(query).sort(sort_order).limit(1)
        else:
            # If there are no results, there must not be a run from the current os codename.
            # Remove that requirement.
            del query['os_version_codename']
            logger.log('INFO: No prior fs permissions from this OS codename found. Relaxing that constraint.')

            if db.count_documents(query):
                results = db.find(query).sort(sort_order).limit(1)
            else:
                logger.log('INFO: No suitable previous fs permissions found')
                return '', '<none>'

        result = next(results)

        log_version_info(
            logger,
            label,
            result['os_version_codename'],
            result['os_version_full'],
            result['date'],
            result['_id']
        )

        return strip_headers(result['fs_permissions']), result['os_version_full']

    os_version = OsVersion()

    query_build = lambda build: {
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

    # The "basis" manifest is from the latest version with a lesser MAJOR.MINOR.PATCH
    basis_manifest, basis_version = run_query('basis', query_build(0))
    # The "recent" manifest is from the latest version, likely the last run with the same MAJOR.MINOR.PATCH
    recent_manifest, recent_version = run_query('recent', query_build(os_version.build))

    # Keep this log in first line to help streak indexer group results. Overall hash will be populated at end.
    logger.prefix_log(f'INFO: fs_permissions_diff: current against {basis_version} ')
    # Keep this log in second line to avoid using it for grouping, but keep it for tracking.
    # Individual hashes to be populated at end.
    logger.prefix_log(f'INFO:   and {recent_version} ')

    return basis_manifest, recent_manifest

def prepare_manifest_for_diff(manifest):
    reader = csv.DictReader(manifest.splitlines(), fieldnames=fs_manifest_columns, delimiter='\t')
    return {row['path']: row for row in reader}

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

def parse_args():
    parser = argparse.ArgumentParser(description='diff fs permissions with previous')
    parser.add_argument('--server', required=True, help='Mongo server hostname')
    parser.add_argument('--user', required=True, help='Mongo server username')
    parser.add_argument('--password', required=True, help='Mongo server password')

    return parser.parse_args()

def main():
    logger = fs_permissions_shared.Logger()
    args = parse_args()
    db = DB(args.server, args.user, args.password)
    basis_fs_manifest, recent_fs_manifest = get_old_fs_manifests(db, logger)

    fs_manifest = get_fs_manifest()
    upload_manifest(db, fs_manifest, logger)

    result = diff_manifests(fs_manifest, basis_fs_manifest, recent_fs_manifest, logger)

    logger.report()
    if result:
        sys.exit(os.EX_OK)
    else:
        sys.exit(os.EX_SOFTWARE)

if __name__ == '__main__':
    main()

