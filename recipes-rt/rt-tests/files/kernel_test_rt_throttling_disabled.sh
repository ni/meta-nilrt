#!/bin/bash

if [ $(sysctl -n kernel.sched_rt_runtime_us) -ne -1 ]; then
    echo "FAIL: kernel was configured with RT throttling enabled"
    exit 1
fi

# now we do a jitter test to specifically verify that throttling is disabled
./test_throttling_jitter
if [ $? -ne 0 ]; then
    echo "FAIL: test_throttling_jitter failed (failure output is above)"
    exit 1
fi

echo "PASS: RT throttling is disabled"
