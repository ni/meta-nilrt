#!/bin/bash

if [ $(uname -a | grep 'PREEMPT RT' | wc -l) -eq 0 ]; then
    echo "FAIL: kernel was not configured to use PREEMPT_RT"
    exit 1
fi

echo "PASS: PREEMPT_RT succesfully detected"
