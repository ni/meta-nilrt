#!/bin/bash
source ./run-cyclictest

# start background network load
source ./iperf.cfg
if [ -z "$IPERF_SERVER" ]; then
    echo "Warning: iperf server not configured; skipping iperf based network load test."
    echo "Edit `pwd`/iperf.cfg file to configure a server to connect to for this test."
    echo "SKIP: test_kernel_cyclictest_iperf"
    exit 77
fi

if [ ! -z "$IPERF_PORT" ]; then
    iperf3 -c "$IPERF_SERVER" -p "$IPERF_PORT" -t 1 > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        echo "ERROR: iperf server not reachable; skipping iperf based network load test."
        echo "SKIP: test_kernel_cyclictest_iperf"
        exit 77
    fi
    iperf3 -c "$IPERF_SERVER" -p "$IPERF_PORT" -t 36000 --logfile "$LOG_DIR/iperf-`date +'%Y_%m_%d-%H_%M_%S'`.log" &
else
    iperf3 -c "$IPERF_SERVER" -t 36000 --logfile "$LOG_DIR/iperf-`date +'%Y_%m_%d-%H_%M_%S'`.log" &
fi

# measure the system latency under network load
run_cyclictest "iperf"

# clean-up
killall -INT iperf3 > /dev/null 2>&1

exit $CYCLICTEST_RESULT
