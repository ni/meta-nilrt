#!/bin/bash

#test if all up net interfaces have a link local route

./test_kernel_hrtimers
if [ $? -ne 0 ]; then
    echo "FAIL: kernel hrtimers clock latency is below threshold"
    exit 1
fi

echo "PASS: kernel hrtimers clock latency"
