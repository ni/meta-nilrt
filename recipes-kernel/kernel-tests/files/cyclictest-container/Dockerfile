FROM ubuntu:latest

# Install rt-tests, sudo, etc
RUN DEBIAN_FRONTEND=noninteractive apt update && apt install -y \
    rt-tests \
    python3 \
    python3-pip \
    dmidecode \
    grub-common

# Need to send to InfluxDB
RUN pip install influxdb

# Script to call "run_cyclictest" from outside Docker container
ADD --chmod=0775 call_run_ct.sh ./call_run_ct.sh

