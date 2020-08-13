#!/bin/bash
source ./run-cyclictest

# start background scheduler load
hackbench -l 36000000 -g 10 2>/dev/null >"$LOG_DIR/hackbench-`date +'%Y_%m_%d-%H_%M_%S'`.log" &

# measure the system latency under scheduler load
run_cyclictest "hackbench"

# clean-up
killall -INT hackbench > /dev/null 2>&1

exit $CYCLICTEST_RESULT
