#!/bin/bash

./test_exe_cap_support
if [ $? -ne 0 ]; then
    echo "FAIL: test_exe_cap_support failed"
    exit 1
fi

./test_proc_cap_support
if [ $? -ne 0 ]; then
    echo "FAIL: test_proc_cap_support failed"
    exit 1
fi

echo "PASS: kernel capabilities support"
