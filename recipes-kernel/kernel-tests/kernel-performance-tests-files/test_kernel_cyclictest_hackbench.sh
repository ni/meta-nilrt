#!/bin/bash
source ./run-cyclictest

# start background scheduler load
./hackbench-load start

# measure the system latency under scheduler load
run_cyclictest "hackbench"

# clean-up
./hackbench-load stop

exit $CYCLICTEST_RESULT
