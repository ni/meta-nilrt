import argparse
import csv
import datetime
import hashlib
import mmap
import os
import re
import subprocess
import sys

class Logger:
    prefix_logs = []
    logs = []
    def log(self, log):
        self.logs.append(log)

    def prefix_log(self, log):
        self.prefix_logs.append(log)

    def report(self):
        for log in self.prefix_logs:
            print(log)
        for log in self.logs:
            print(log)

def run_cmd(cmd):
    output = subprocess.check_output(cmd)
    return output.decode('utf-8')

# Columns: Path, Mode, User, Group, Link Target
fs_manifest_format = '%p\t%M\t%u\t%g\t%l'
def get_fs_manifest():
    search_dirs = ['/bin', '/boot', '/etc', '/lib', '/lib64', '/sbin', '/usr', '/var']
    omit_dirs = ['/etc/natinst/niskyline/Data/Assets/Cache', '/lib/modules', '/var/cache', '/var/run', '/var/tmp', '/var/volatile']

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
    entries = {}
    for entry in fs_manifest.splitlines():
        entry_data = entry.split('\t')
        entries[entry_data[0]] = {
            'mode': entry_data[1],
            'user': entry_data[2],
            'group': entry_data[3],
            'link': entry_data[4]
        }
    return entries

def log_version_info(logger, label, codename, full_version, date, db_id):
    logger.log(f'INFO: {label} = {codename} {full_version} from {date} with _id {db_id}')

def check_known_manifests(manifest, logger):
    logger.log('###### Checking expected file permissions against actual for known folders')
    failed = False
    with open('known-permissions.csv') as known_perm_file:
        permissions = csv.reader(known_perm_file, delimiter = ',')
        header_row = True
        for row in permissions:
            if header_row:
                header_row = False
                continue

            # Allow executing shell script to get path name.
            # Useful for things like /lib/modules/$(uname -r), for example.
            # Not using regex because multiple ones could cause issues, but
            # equivalent regex is /\$\((\\.|[^\\\)]*\)/
            path = row[0]
            new_path = ''
            building_shell = False
            shell_cmd = ''
            i = 0
            while i < len(path):
                if not building_shell \
                and path[i] == '\\' and i + 1 < len(path):
                    i += 1
                    new_path += path[i]
                elif not building_shell \
                and path[i] == '$' \
                and i + 1 < len(path) and path[i + 1] == '(':
                    i += 1
                    building_shell = True
                    shell_cmd = ''
                elif not building_shell:
                    new_path += path[i]
                elif path[i] == '\\' \
                and i + 1 < len(path):
                    i += 1
                    shell_cmd += path[i]
                elif path[i] == ')':
                    shell_out = run_cmd(shell_cmd.split(' ')).strip()
                    new_path += shell_out
                    building_shell = False
                else:
                    shell_cmd += path[i]
                i += 1
            path = new_path

            # Now, if the manifest has the path, compare permissions
            if path in manifest:
                if manifest[path]['mode'] != row[1]:
                    logger.log(
                        'ERROR: Expected mode \'' + row[1] + '\' but got \''
                            + manifest[path]['mode'] + '\' for path \'' + path + '\''
                    )
                    failed = True
                if manifest[path]['user'] != row[2]:
                    logger.log(
                        'ERROR: Expected user \'' + row[2] + '\' but got \''
                            + manifest[path]['user'] + '\' for path \'' + path + '\''
                    )
                    failed = True
                if manifest[path]['group'] != row[3]:
                    logger.log(
                        'ERROR: Expected group \'' + row[3] + '\' but got \''
                            + manifest[path]['group'] + '\' for path \'' + path + '\''
                    )
                    failed = True
                if manifest[path]['link'] != row[4]:
                    logger.log(
                        'ERROR: Expected link \'' + row[4] + '\' but got \''
                            + manifest[path]['link'] + '\' for path \'' + path + '\''
                    )
                    failed = True
            else:
                logger.log('ERROR: System is missing expected path \'' + path + '\'')
                failed = True
    logger.log('###### End of known file permissions check')
    return failed

def parse_args():
    parser = argparse.ArgumentParser(description='Check fs permissions against expectations')
    return parser.parse_args()

logger = Logger()
args = parse_args()

fs_manifest = get_fs_manifest()
result = check_known_manifests(fs_manifest, logger)
logger.report()

if result:
    sys.exit(os.EX_OK)
else:
    sys.exit(os.EX_SOFTWARE)
