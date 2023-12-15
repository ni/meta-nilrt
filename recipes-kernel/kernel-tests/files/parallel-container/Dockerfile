FROM ubuntu:latest

# Install rt-tests, sudo, etc
RUN DEBIAN_FRONTEND=noninteractive apt update && apt install -y \
    rt-tests \
    fio \
    iperf3 \
    psmisc
