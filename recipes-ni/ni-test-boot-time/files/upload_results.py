#!/usr/bin/env python3
"""Script for uploading the boot time measurement to influxdb"""

import argparse
import logging
import mmap
import re
import sys
import subprocess
from datetime import datetime
from influxdb import InfluxDBClient
from influxdb.exceptions import InfluxDBClientError
from requests.exceptions import ConnectionError

def fw_printenv(key):
    """Extract firmware information (ex. device, serial#) via fw_printenv"""
    try:
        process = subprocess.run(['fw_printenv', key],
                                 check=True,
                                 stdout=subprocess.PIPE)
        value_str = process.stdout.strip().decode("utf-8")
        value = value_str.split('=')[-1]
    except (FileNotFoundError, subprocess.CalledProcessError):
        logging.warning("calling 'fw_printenv %s' failed", key)
        raise
    return value


def get_device():
    """Get the device description for the current system (e.g. cRIO-90xx)"""
    return fw_printenv('DeviceDesc')


def get_serial_number():
    """Get the device serial number; return all-zeros if not present"""
    try:
        serialno = fw_printenv('serial#')
    except (FileNotFoundError, subprocess.CalledProcessError):
        # Non-fatal error, some early development hardware doesn't have a serial# provisioned
        logging.warning("serial number not found, defaulting to 00000000")
        serialno = "00000000"
    return serialno


def get_hostname():
    """Get the device hostname"""
    try:
        process = subprocess.run(['hostname'],
                                 check=True,
                                 stdout=subprocess.PIPE)
        name = process.stdout.strip().decode("utf-8")
    except (FileNotFoundError, subprocess.CalledProcessError):
        logging.warning("failed to retrieve the hostname")
        raise
    return name


def get_kernel_version():
    """Get the kernel version currently running on the system"""
    try:
        process = subprocess.run(['uname', '-r'],
                                 check=True,
                                 stdout=subprocess.PIPE)
        kver_full = process.stdout.strip().decode("utf-8")
        rgx = re.search(r'([0-9]+\.[0-9]+)\.([0-9]+)', kver_full)
        kver = rgx.group(1)
    except (FileNotFoundError, ValueError, AttributeError,
            subprocess.CalledProcessError):
        logging.warning("failed to read the current kernel version")
        raise

    return kver, kver_full


def get_os_version():
    """Extract OS version information from /etc/os-release"""
    try:
        with open('/etc/os-release', 'rb', 0) as file, \
             mmap.mmap(file.fileno(), 0, access=mmap.ACCESS_READ) as mfile:
            rgx = re.search(br'VERSION_ID=([0-9.]+)', mfile)
            version = rgx.group(1).decode()

            rgx = re.search(br'BUILD_ID="([^"]+)"', mfile)
            build = rgx.group(1).decode()

            rgx = re.search(br'VERSION_CODENAME="([^"]+)"', mfile)
            codename = rgx.group(1).decode()
    except (FileNotFoundError, ValueError, AttributeError):
        logging.warning("failed to parse version information from '/etc/os-release'")
        raise

    return version, build, codename


def get_boot_time(log_file):
    """Extract boot time from log file"""
    try:
        with open(log_file, 'rb', 0) as file, \
             mmap.mmap(file.fileno(), 0, access=mmap.ACCESS_READ) as mfile:
            rgx = re.search(br'([0-9.]+)', mfile)
            boot_time = float(rgx.group(1).decode())
    except (FileNotFoundError, ValueError, AttributeError):
        logging.warning("failed to parse boot time from '%s'", log_file)
        raise

    return boot_time


def read_data(path):
    """Compile test data and metadata"""
    controller = get_device()
    serialno = get_serial_number()
    hostname = get_hostname()
    kernel_version, kernel_full_version = get_kernel_version()
    distro_version, distro_build, distro_name = get_os_version()
    boot_time = get_boot_time(path)

    print("boot time:", boot_time, "(s)")

    data = {
        "measurement": "boot_performance",
        "tags": {
            "controller": controller,
            "serialno": serialno,
            "hostname": hostname,
            "kernel_version": kernel_version,
            "kernel_full_version": kernel_full_version,
            "distro_version": distro_version,
            "distro_build": distro_build,
            "distro_name": distro_name,
        },
        "fields": {
            "boot_time": boot_time,
        },
        "time": str(datetime.now())
    }
    return data


def upload_results(data, server, server_port):
    """Upload results to influxdb instance"""
    try:
        influxdb = InfluxDBClient(host=server, port=server_port)
        influxdb.switch_database("rtos_boot_performance")
        if not influxdb.write_points(data):
            logging.warning("failed to write data points to influxdb")
        influxdb.close()
    except (InfluxDBClientError, ConnectionError):
        logging.warning("failed to connect to influxdb server '%s:%u'",
                        server, server_port)
        raise


parser = argparse.ArgumentParser(
    description="Upload boot time measurement.")
parser.add_argument("-i", "--input", required=True,
                    help="boot time log input file")
parser.add_argument("-s", "--server", required=True,
                    help="influxdb server")
parser.add_argument("-p", "--port", type=int, default=8086,
                    help="influxdb server port")
args = parser.parse_args()

results = []
try:
    result = read_data(args.input.strip())
except (FileNotFoundError, AttributeError, ValueError,
        subprocess.CalledProcessError):
    logging.error('failed to parse test results or metadata')
    sys.exit(1)
results.append(result)

try:
    upload_results(results, args.server.strip(), args.port)
except (InfluxDBClientError, ConnectionError):
    logging.error('failed to upload test results')
    sys.exit(1)
