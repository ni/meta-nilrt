#!/bin/sh
source ./run-cyclictest

# start background disk I/O load
mkdir -p .fio
fio fio.cfg --ioengine="sync" --runtime="$TEST_DURATION" --ramp_time=1m --output="results/fio-sync-`date +'%Y_%m_%d-%H_%M_%S'`.log" > /dev/null &

# measure the system latency under disk load
run_cyclictest "fio"

# clean-up
killall -INT fio > /dev/null 2>&1
rm -rf .fio

exit $CYCLICTEST_RESULT
