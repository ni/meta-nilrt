import argparse
from bson.binary import Binary
import datetime
import difflib
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

def run_cmd(cmd):
    output = subprocess.check_output(cmd).strip()
    return output.decode('utf-8')

def get_device_desc():
    cmdoutput = run_cmd(['fw_printenv', 'DeviceDesc'])
    return cmdoutput.split('=')[1]

class KernelVersion:
    def __init__(self):
        self.uname = run_cmd(['uname', '-r'])
        kernel_version = re.search(r'((\d+\.\d+)\.\d+-rt\d+)(-next)?', self.uname)
        self.full = kernel_version.group(1)
        self.major_minor = kernel_version.group(2)
        self.type = 'next' if kernel_version.group(3) else 'current'

def get_architecture():
    return run_cmd(['uname', '-m'])

def get_os_version():
    with open('/etc/os-release', 'rb') as file, mmap.mmap(file.fileno(), 0, access=mmap.ACCESS_READ) as mfile:
        build_id = re.search(br'BUILD_ID=\"(.*)\"', mfile)
        return build_id.group(1).decode()

def get_dmesg_log():
    dmesg_log = run_cmd(['dmesg'])
    return dmesg_log

def upload_log(db, dmesg_log):
    data = {}
    kernel_version = KernelVersion()
    data['kernel_version_full'] = kernel_version.full
    data['kernel_version_major_minor'] = kernel_version.major_minor
    data['kernel_type'] = kernel_version.type
    data['device_desc'] = get_device_desc()
    data['architecture'] = get_architecture()
    data['os_version'] = get_os_version()
    data['date'] = str(datetime.datetime.now())

    header = ''
    for key, val in data.items():
        header = header + '# {}: {}\n'.format(key, val)

    data['dmesg_log'] = header + dmesg_log

    record = db.insert(data)
    print('INFO: Uploaded dmesg log of "{}" kernel {} from {} with _id {}'.format(data['kernel_type'], data['kernel_version_full'], data['date'], record.inserted_id))

def strip_headers(dmesg_log):
    output = ''
    for line in dmesg_log.splitlines():
        if not line.startswith('#'):
            output = output + line + '\n'
    return output

def get_old_dmesg_log(db):
    query = {}
    kernel_version = KernelVersion()
    query['kernel_version_full'] = {'$ne': kernel_version.full}
    query['device_desc'] = get_device_desc()
    query['kernel_version_major_minor'] = kernel_version.major_minor

    if db.count_documents(query):
        results = db.find(query).sort('date', pymongo.DESCENDING).limit(1)
    else:
        # If there aren't any results matching the kernel major minor version, broaden the search
        # but limit it to same kernel type
        del query['kernel_version_major_minor']
        query['kernel_type'] = kernel_version.type

        if db.count_documents(query):
            results = db.find(query).sort('date', pymongo.DESCENDING).limit(1)
        else:
            print('INFO: No suitable previous dmesg log found')
            return ''

    result = next(results)
    print('INFO: Using previous dmesg log of "{}" kernel {} from {} with _id {}'.format(result['kernel_type'], result['kernel_version_full'], result['date'], result['_id']))
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
        [r'NODE_DATA\(0\) allocated \[mem .*\]', 'NODE_DATA(0) allocated [mem ]']
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

def diff_logs(current_log, old_log):
    current_log = prepare_log_for_diff(current_log)
    old_log = prepare_log_for_diff(old_log)
    diff = list(difflib.unified_diff(old_log, current_log, n=0))
    if diff:
        print('INFO: Starting diff')
        for line in diff:
            print(line)
        print('INFO: End of diff')
        return False

    print('INFO: Empty diff')
    return True

def parse_args():
    parser = argparse.ArgumentParser(description='diff dmesg log with a previous log')
    parser.add_argument('--server', required=True, help='Mongo server hostname')
    parser.add_argument('--user', required=True, help='Mongo server username')
    parser.add_argument('--password', required=True, help='Mongo server password')

    return parser.parse_args()

args = parse_args()
db = DB(args.server, args.user, args.password)
old_dmesg_log = get_old_dmesg_log(db)

dmesg_log = get_dmesg_log()
upload_log(db, dmesg_log)

result = diff_logs(dmesg_log, old_dmesg_log)

if result:
    sys.exit(os.EX_OK)
else:
    sys.exit(os.EX_SOFTWARE)
