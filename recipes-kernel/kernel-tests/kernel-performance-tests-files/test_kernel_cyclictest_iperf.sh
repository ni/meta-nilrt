#!/bin/sh
source ./run-cyclictest

# start background network load
source ./iperf.cfg
if [ -z "$IPERF_SERVER" ]; then
    echo "SKIP: iperf server not configured, skipping iperf based network load test."
    echo "Edit `pwd`/iperf.cfg file to configure a server to connect to for this test."
    exit 77
fi

if [ ! -z "$IPERF_PORT" ]; then
	iperf3 -c "$IPERF_SERVER" -p "$IPERF_PORT" -t 36000 --logfile "results/iperf-`date +'%Y_%m_%d-%H_%M_%S'`.log" &
    else
	iperf3 -c "$IPERF_SERVER" -t 36000 --logfile "results/iperf-`date +'%Y_%m_%d-%H_%M_%S'`.log" &
fi

# measure the system latency under network load
run_cyclictest "iperf"

# clean-up
killall -INT iperf3 > /dev/null 2>&1

exit $CYCLICTEST_RESULT
