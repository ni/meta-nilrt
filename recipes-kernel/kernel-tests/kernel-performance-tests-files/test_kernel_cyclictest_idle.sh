#!/bin/bash
source ./run-cyclictest

# measure the system latency with just background noise (i.e. idle system with no explicit load)
run_cyclictest "idle"

exit $CYCLICTEST_RESULT
