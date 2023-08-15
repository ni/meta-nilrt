import datetime
import subprocess
import os
import argparse
import sys
import logging

logging.basicConfig(level = logging.INFO)

TEST_DESC = 'Test to see that the file size of the base system image is not getting too large.'

# Filled in with CLI args
X86_MAX_SIZE_EOY = -1
ARM_MAX_SIZE_EOY = -1

def get_max_size():
    arch = run_cmd([ 'uname', '-m' ]).strip()
    if arch == 'x86_64':
        return X86_MAX_SIZE_EOY
    elif arch == 'arm':
        return ARM_MAX_SIZE_EOY
    else:
        logging.error(f'ERROR: Failed to get size limit! Unknown architecture: "{arch}"')
        return -1

def run_cmd(cmd):
    output = subprocess.run(cmd, check = False, capture_output = True)
    return output.stdout.decode('utf-8')

def du_path(path):
    du_result = run_cmd([ 'du', '-hs', '-BM', path ]) # Format: "####M      /"
    size_str = du_result.split('\t')[0].strip()[:-1]
    return int(size_str)

def get_disk_used():
    root_size = du_path('/')
    boot_size = du_path('/boot')
    return root_size + boot_size

def test_disk_size():
    logging.info('###### Testing install size against estimated max growth')
    
    size_limit = get_max_size()
    logging.info(f'Size Limit: {size_limit}')

    actual_size = get_disk_used()
    logging.info(f'Actual Size: {actual_size}')

    success = actual_size < size_limit

    logging.info('###### Finished testing install size')
    return success

def parse_args():
    parser = argparse.ArgumentParser(description = TEST_DESC)
    parser.add_argument(
        '--x86_max_size', required = True, type = int,
        help = 'Current size limit for x86 devices'
    )
    parser.add_argument(
        '--arm_max_size', required = True, type = int,
        help = 'Current size limit for ARM devices'
    )
    return parser.parse_args()

def main():
    global X86_MAX_SIZE_EOY
    global ARM_MAX_SIZE_EOY
    args = parse_args()
    X86_MAX_SIZE_EOY = args.x86_max_size
    ARM_MAX_SIZE_EOY = args.arm_max_size

    result = test_disk_size()

    if result:
        sys.exit(os.EX_OK)
    else:
        sys.exit(os.EX_SOFTWARE)

if __name__ == '__main__':
    main()

