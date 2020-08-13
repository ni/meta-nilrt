#!/bin/bash
source ./run-cyclictest

# start background disk I/O load
mkdir -p /var/cache/fio
fio fio.cfg --ioengine="sync" --runtime="$TEST_DURATION" --ramp_time=1m --output="$LOG_DIR/fio-sync-`date +'%Y_%m_%d-%H_%M_%S'`.log" > /dev/null &

# measure the system latency under disk load
run_cyclictest "fio"

# clean-up
killall -INT fio > /dev/null 2>&1
rm -rf /var/cache/fio

exit $CYCLICTEST_RESULT
