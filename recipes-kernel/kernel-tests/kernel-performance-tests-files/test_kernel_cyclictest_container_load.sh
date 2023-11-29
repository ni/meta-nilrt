#!/bin/bash
source ./run-cyclictest
source ./run-container-load

# start background container load
setup_container_load
start_container_load &

# measure the system latency under container load
run_cyclictest "container"

# clean-up
kill $(jobs -p) 2>/dev/null

exit $CYCLICTEST_RESULT
