FROM ubuntu:latest

# Install rt-tests, sudo, etc
RUN DEBIAN_FRONTEND=noninteractive apt update && apt install -y \
    rt-tests \
    python3 \
    python3-pip \
    dmidecode \
    grub-common \
    python3-influxdb
