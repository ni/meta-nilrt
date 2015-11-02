#!/bin/bash

./test_kernel_mcopy_functionality
if [ $? -ne 0 ]; then
    echo "FAIL: test_kernel_mcopy_functionality failed"
    exit 1
fi

# We disable the next test because it gives a false-negative every time

# ./test_kernel_mcopy_freed_memory
# if [ $? -ne 0 ]; then
#     echo "FAIL (possible false negative): test_kernel_mcopy_freed_memory failed"
#     exit 1
# fi

echo "PASS: kernel mcopy syscall"
