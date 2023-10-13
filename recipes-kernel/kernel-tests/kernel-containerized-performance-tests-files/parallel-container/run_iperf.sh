#!/bin/sh
cd /ptests
source run-cyclictest
source /home/admin/.iperf.info
if [ ! -z "${IPERF_PORT}" ]; then
    iperf3 -c "${IPERF_SERVER}" -p "${IPERF_PORT}" -t 36000 \
        --logfile "${LOG_DIR}/iperf-`date +'%Y_%m_%d-%H_%M_%S'`.log"
else
    iperf3 -c "${IPERF_SERVER}" -t 36000 \
        --logfile "${LOG_DIR}/iperf-`date +'%Y_%m_%d-%H_%M_%S'`.log"
fi

