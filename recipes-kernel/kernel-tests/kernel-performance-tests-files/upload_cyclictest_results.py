#!/usr/bin/env python3
"""Script for uploading a cyclictest log to influxdb"""

import argparse
import logging
import mmap
import re
import subprocess
import sys
from pathlib import Path
from datetime import datetime
from influxdb import InfluxDBClient, exceptions
import requests.exceptions


def get_current_kernel_version():
    """Get the kernel version currently running on the system"""
    try:
        process = subprocess.run(['uname', '-r'],
                                 check=True,
                                 stdout=subprocess.PIPE)
        kver_full = process.stdout.strip().decode("utf-8")
        rgx = re.search(r'([0-9]+\.[0-9]+)\.([0-9]+)', kver_full)
        kver = rgx.group(1)
    except (FileNotFoundError, AttributeError):
        logging.warning("failed to read the current kernel version")
        raise

    return kver, kver_full


def get_kernel_version(path):
    """Get the kernel version used in cyclictest test

    Read 'major.minor' and 'major.minor.patch-rt' kernel version strings from
    cyclictest log if found or current running system otherwise.

    """
    try:
        with open(path, 'rb', 0) as file, \
             mmap.mmap(file.fileno(), 0, access=mmap.ACCESS_READ) as mfile:
            kernel = re.search(br'# Kernel: Linux ([^ ]+) ([0-9]+)\.([0-9]+)\.'
                               br'([0-9]+-rc[0-9]+-rt[0-9]+|[0-9]+-rt[0-9]+)',
                               mfile)
            # ignore build machine name, i.e. group(1)
            major = kernel.group(2).decode()
            minor = kernel.group(3).decode()
            patch = kernel.group(4).decode()
            version = major + '.' + minor
            full_version = version + '.' + patch
    except (ValueError, AttributeError):
        logging.warning('kernel version information missing from \'%s\' '
                        'retriving from current system.', path)
        version, full_version = get_current_kernel_version()
    return version, full_version


def get_current_device():
    """Get the device description for the current system (e.g. cRIO-90xx)"""
    try:
        process = subprocess.run(['fw_printenv', 'DeviceDesc'],
                                 check=True,
                                 stdout=subprocess.PIPE)
        dev_str = process.stdout.strip().decode("utf-8")
        dev = dev_str.split('=')[-1]
    except FileNotFoundError:
        logging.warning("failed to read the current device type")
        raise

    return dev


def get_device(path):
    """Get the device name used in cyclictest test

    Read the device name stored in the cyclictest log file or if not available
    retrieve the current device name.

    """
    try:
        with open(path, 'rb', 0) as file, \
             mmap.mmap(file.fileno(), 0, access=mmap.ACCESS_READ) as mfile:
            rgx = re.search(br'# Device: ([a-zA-z]+-[0-9]+)', mfile)
            device = rgx.group(1).decode()
    except (ValueError, AttributeError):
        logging.warning('device information missing from \'%s\' '
                        'retriving from current system.', path)
        device = get_current_device()
    return device


def get_test_params(path):
    """Get the test name and datetime

    Extract the test name and date/time from cyclictest log name.

    """
    try:
        rgx = re.search(r'[^_]*cyclictest-([^-]+)-([^-]+)-([^\.]+).log',
                        path.name)
        test = rgx.group(1)
        date = rgx.group(2).replace('_', '-')
        time = rgx.group(3).replace('_', ':')
        date_time = datetime.fromisoformat(date + ' ' + time)
    except AttributeError:
        logging.warning('failed to parse test parameters '
                        '(i.e. test name, date, time)')
        raise

    return test, date_time


def get_test_results(path):
    """Extract min, avg, max latency from cyclictest log"""
    try:
        with open(path, 'rb', 0) as file, \
             mmap.mmap(file.fileno(), 0, access=mmap.ACCESS_READ) as mfile:
            rgx = re.search(br'# Max Latencies: ([^\n]+)', mfile)
            max_list = list(map(int, rgx.group(1).decode().split()))
            max_latency = max_list[-1]

            rgx = re.search(br'# Min Latencies: ([^\n]+)', mfile)
            min_list = list(map(int, rgx.group(1).decode().split()))
            min_latency = min(min_list)

            rgx = re.search(br'# Avg Latencies: ([^\n]+)', mfile)
            avg_list = list(map(int, rgx.group(1).decode().split()))
            avg_latency = sum(avg_list) / len(avg_list)
    except (ValueError, AttributeError):
        logging.warning("failed to parse test results")
        raise

    return min_latency, avg_latency, max_latency

def get_distro_info():
    """Extract distro version and codename from os-release and return them as a tuple

    Returns 'unknown' for either in case of file read exception or missing fields.

    """
    os_release_stmts = []
    try:
        with open('/etc/os-release') as os_release_file:
            os_release_stmts = os_release_file.readlines()
    except:
        return ('unknown', 'unknown')
    pairs = dict(map(lambda e: e.split('='), os_release_stmts))
    version = pairs.get('VERSION_ID', 'unknown').strip()
    name = pairs.get('DISTRO_CODENAME', 'unknown').strip()[1:-1]
    return (version, name)

def read_data(path):
    """Read test data and metadata from a cyclictest log file"""
    version, full_version = get_kernel_version(path)
    controller = get_device(path)
    test, time = get_test_params(path)
    min_latency, avg_latency, max_latency = get_test_results(path)
    distro_version, distro_name = get_distro_info()

    data = {
        "measurement": "latency",
        "tags": {
            "controller": controller,
            "test": test,
            "kernel_version": version,
            "kernel_full_version": full_version,
            "distro_version": distro_version,
            "distro_name": distro_name
        },
        "fields": {
            "min_latency": float(min_latency),
            "avg_latency": float(avg_latency),
            "max_latency": float(max_latency),
        },
        "time": time
    }
    return data


def upload_results(data, server, server_port):
    """Upload results to influxdb instance"""
    try:
        influxdb = InfluxDBClient(host=server, port=server_port)
        influxdb.switch_database("rtos_kernel_performance")
        if not influxdb.write_points(data):
            logging.warning("failed to write data points to influxdb")
        influxdb.close()
    except (exceptions.InfluxDBClientError,
            requests.exceptions.ConnectionError) as err:
        logging.warning('influxdb error: %s', err)


parser = argparse.ArgumentParser(
    description="Parse and upload cyclictest results.")
parser.add_argument("-i", "--input", required=True,
                    help="cyclictest log input file")
parser.add_argument("-s", "--server", required=True,
                    help="influxdb server")
parser.add_argument("-p", "--port", type=int, default=8086,
                    help="influxdb server port")
args = parser.parse_args()

log_file = Path(args.input.strip())
if not log_file.is_file():
    logging.error('input file %s does not exist!', log_file)
    sys.exit(1)

try:
    result = read_data(log_file)
except (FileNotFoundError, AttributeError, ValueError):
    logging.error('skipping malformed or incomplete test results at: %s',
                  log_file)
    sys.exit(1)

results = []
results.append(result)
upload_results(results, args.server.strip(), args.port)
