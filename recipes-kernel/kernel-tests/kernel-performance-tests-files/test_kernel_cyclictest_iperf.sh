#!/bin/bash
source ./run-cyclictest

./iperf-load check || exit $?
./iperf-load start

# measure the system latency under network load
run_cyclictest "iperf"

./iperf-load stop

exit $CYCLICTEST_RESULT
