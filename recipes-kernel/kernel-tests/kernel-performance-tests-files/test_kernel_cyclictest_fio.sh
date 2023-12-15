#!/bin/bash
source ./run-cyclictest

# start background disk I/O load
./fio-load start

# measure the system latency under disk load
run_cyclictest "fio"

# clean-up
./fio-load stop

exit $CYCLICTEST_RESULT
