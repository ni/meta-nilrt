import argparse
from bson.binary import Binary
import datetime
import difflib
import hashlib
import mmap
import os
import pymongo
import re
import subprocess
import sys

class DB:
    mongo_db_name = 'rtos'
    mongo_db_collection_name = 'dmesg_logs'

    def __init__(self, server, user, password):
        mongo_client = pymongo.MongoClient("mongodb://{user}:{pwd}@{server}".format(
            user=args.user, pwd=args.password, server=args.server
            ),
            w=1)
        db = mongo_client[self.mongo_db_name]
        self.dmesg_logs_collection = db[self.mongo_db_collection_name]

    def insert(self, data):
        return self.dmesg_logs_collection.insert_one(data)

    def find(self, query):
        return self.dmesg_logs_collection.find(query)

    def count_documents(self, query):
        return self.dmesg_logs_collection.count_documents(query)

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
    output = subprocess.check_output(cmd).strip()
    return output.decode('utf-8')

def get_device_desc():
    cmdoutput = run_cmd(['fw_printenv', 'DeviceDesc'])
    return cmdoutput.split('=')[1]

class SemanticVersion:
    def __init__(self, versionString):
        # Regex below copied from https://semver.org/.
        version = re.search(r'^(?P<major>0|[1-9]\d*)\.(?P<minor>0|[1-9]\d*)\.(?P<patch>0|[1-9]\d*)(?:-(?P<prerelease>(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+(?P<buildmetadata>[0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$', versionString)
        self.full = version.group()
        self.major = int(version.group('major'))
        self.minor = int(version.group('minor'))
        self.patch = int(version.group('patch'))
        self.prerelease = version.group('prerelease')
        self.build_metadata = version.group('buildmetadata')

class KernelVersion(SemanticVersion):
    def __init__(self):
        version =run_cmd(['uname', '-r'])
        SemanticVersion.__init__(self, version)
        self.type = 'next' if self.build_metadata else 'current'
        prerelease_version = int(re.search(r'0|[1-9]\d*', self.prerelease).group())
        self.version_dict = { 'major': self.major, 'minor': self.minor, "patch": self.patch, 'prerelease': prerelease_version }

class OsVersion:
    def __init__(self):
        with open('/etc/os-release', 'rb') as file, mmap.mmap(file.fileno(), 0, access=mmap.ACCESS_READ) as mfile:
            build_id = re.search(br'BUILD_ID=\"((\d+\.\d+).*)\"', mfile)
            self.full = build_id.group(1).decode()
            self.major_minor = build_id.group(2).decode()

def get_architecture():
    return run_cmd(['uname', '-m'])

def get_dmesg_log():
    dmesg_log = run_cmd(['dmesg'])
    return dmesg_log

def upload_log(db, dmesg_log, logger):
    data = {}
    kernel_version = KernelVersion()
    os_version = OsVersion()
    data['kernel_version_full'] = kernel_version.full
    data['kernel_version'] = kernel_version.version_dict
    data['kernel_type'] = kernel_version.type
    data['device_desc'] = get_device_desc()
    data['architecture'] = get_architecture()
    data['os_version'] = os_version.full
    data['os_version_major_minor'] = os_version.major_minor
    data['date'] = str(datetime.datetime.now())

    header = ''
    for key, val in data.items():
        header = header + '# {}: {}\n'.format(key, val)

    data['dmesg_log'] = header + dmesg_log

    record = db.insert(data)
    logger.log('INFO: Uploaded dmesg log of "{}" kernel {} from {} with _id {}'.format(data['kernel_type'], data['kernel_version_full'], data['date'], record.inserted_id))

def strip_headers(dmesg_log):
    output = ''
    for line in dmesg_log.splitlines():
        if not line.startswith('#'):
            output = output + line + '\n'
    return output

def get_old_dmesg_log(db, logger):
    query = {}
    kernel_version = KernelVersion()
    os_version = OsVersion()
    query['kernel_version'] = { '$lt': kernel_version.version_dict }
    query['device_desc'] = get_device_desc()
    query['kernel_version.major'] = kernel_version.major
    query['kernel_version.minor'] = kernel_version.minor
    query['os_version_major_minor'] = os_version.major_minor

    if db.count_documents(query):
        results = db.find(query).sort('date', pymongo.DESCENDING).limit(1)
    else:
        # If there are no results, there may not be a run from the current os version.
        # Remove that requirement.
        del query['os_version_major_minor']
        logger.log('INFO: No prior logs from this OS Version found. Using previous version.')

        if db.count_documents(query):
            results = db.find(query).sort('date', pymongo.DESCENDING).limit(1)
        else:
            # If there still aren't any results matching the kernel major minor version, broaden the
            # search but limit it to same kernel type
            del query['kernel_version.major']
            del query['kernel_version.minor']
            query['kernel_type'] = kernel_version.type

            if db.count_documents(query):
                results = db.find(query).sort('date', pymongo.DESCENDING).limit(1)
            else:
                # Keep this log in first line to help streak indexer group results. Diff hash will be populated at end.
                logger.first_log('INFO: dmesg_diff: {} against <empty>, diff hash '.format(kernel_version.full))

                logger.log('INFO: No suitable previous dmesg log found')
                return ''

    result = next(results)

    # Keep this log in first line to help streak indexer group results. Diff hash will be populated at end.
    logger.first_log('INFO: dmesg_diff: {} against older log of {}, diff hash '.format(kernel_version.full, result['kernel_version_full']))

    logger.log('INFO: Using previous dmesg log of "{}" kernel {} from {} with _id {}'.format(result['kernel_type'], result['kernel_version_full'], result['date'], result['_id']))
    dmesg_log = result['dmesg_log']
    return strip_headers(dmesg_log)

def strip_timestamps(log):
    return re.sub(r'^\[.+?\] ', '', log, flags=re.MULTILINE)

replacement_patterns = [
        [r'Linux version \S+', 'Linux version '],
        [r'root=PARTUUID=.+? ', 'root=PARTUUID= '],
        [r'setting system clock to .*', 'setting system clock to '],
        [r'sched_clock: Marking stable.*', 'sched_clock: Marking stable '],
        [r'udevd\[\d+\]: ', 'udevd[]: '],
        [r'ACPI: Core revision \d+', 'ACPI: Core revision '],
        [r'Built 1 zonelists, mobility grouping on.  Total pages: \d+', 'Built 1 zonelists, mobility grouping on.  Total pages: '],
        [r'Freeing unused kernel image .* memory: \S+', 'Freeing unused kernel image : '],
        [r'SMP PREEMPT_RT .* UTC \d+', 'SMP PREEMPT_RT '],
        [r'Memory: .* available \(.*\)', 'Memory: available ()'],
        [r'Write protecting the kernel read-only data: \S+', 'Write protecting the kernel read-only data: '],
        [r'ftrace: allocated \d+ pages with \d+ groups', 'ftrace: allocated pages with groups'],
        [r'ftrace: allocating \d+ entries in \d+ pages', 'ftrace: allocating entries in pages'],
        [r'pcpu-alloc: .*alloc=\S+', 'pcpu-alloc: '],
        [r'percpu: Embedded .*', 'percpu: Embedded '],
        [r'tcp_listen_portaddr_hash hash table entries: .*', 'tcp_listen_portaddr_hash hash table entries: '],
        [r'hash table entries: .*', 'hash table entries: '],
        [r'succeeded in \d+ usecs', 'succeeded in usecs'],
        [r'audit\(\d+\.\d+:\d+\):', 'audit():'],
        [r', CDC EEM Device, \S+', ', CDC EEM Device, '],
        [r' HOST MAC \S+', ' HOST MAC '],
        [r'NODE_DATA\(0\) allocated \[mem .*\]', 'NODE_DATA(0) allocated [mem ]'],
        [r'ACPI: SSDT 0x.*', 'ACPI: SSDT 0x']
]

def strip_known_differences(log):
    for pattern in replacement_patterns:
        log = re.sub(pattern[0], pattern[1], log, flags=re.MULTILINE)
    return log

def prepare_log_for_diff(log):
    log = strip_timestamps(log)
    log = strip_known_differences(log)
    log = log.splitlines()
    log.sort()
    return log

def diff_logs(current_log, old_log, logger):
    current_log = prepare_log_for_diff(current_log)
    old_log = prepare_log_for_diff(old_log)
    diff = list(difflib.unified_diff(old_log, current_log, n=0))
    if diff:
        # Add diff hash to first log
        logger.logs[0] += hashlib.md5(''.join(diff).encode('utf-8')).hexdigest()

        logger.log('INFO: Starting diff')
        for line in diff:
            logger.log(line)
        logger.log('INFO: End of diff')
        return False

    # Add diff hash to first log
    logger.logs[0] += '0'

    logger.log('INFO: Empty diff')
    return True

def parse_args():
    parser = argparse.ArgumentParser(description='diff dmesg log with a previous log')
    parser.add_argument('--server', required=True, help='Mongo server hostname')
    parser.add_argument('--user', required=True, help='Mongo server username')
    parser.add_argument('--password', required=True, help='Mongo server password')

    return parser.parse_args()

logger = Logger()
args = parse_args()
db = DB(args.server, args.user, args.password)
old_dmesg_log = get_old_dmesg_log(db, logger)

dmesg_log = get_dmesg_log()
upload_log(db, dmesg_log, logger)

result = diff_logs(dmesg_log, old_dmesg_log, logger)

logger.report()

if result:
    sys.exit(os.EX_OK)
else:
    sys.exit(os.EX_SOFTWARE)
