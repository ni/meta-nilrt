FROM ubuntu:latest

# Install rt-tests, sudo, etc
RUN DEBIAN_FRONTEND=noninteractive apt update && apt install -y \
    rt-tests \
    fio \
    iperf3 \
    psmisc

# Scripts to start loads from outside container
ADD --chmod=0775 run_hackbench.sh ./run_hackbench.sh
ADD --chmod=0775 run_fio.sh ./run_fio.sh
ADD --chmod=0775 run_iperf.sh ./run_iperf.sh

