#!/bin/bash

# The test is checking:
#	1) "ulimit -s" should return 512 (not 8192)
#	2) pthread attribute should return 512k as the default size assuming if the
#	   user does not attempt to set it.

EXPECTED_STACK_SIZE=512

STACK_SIZE_KB=$(ulimit -s)
if [ $STACK_SIZE_KB -ne $EXPECTED_STACK_SIZE ]; then
    echo "FAIL:" $(basename $0): "user stack size (ulimit) is" $STACK_SIZE_KB "KB, expected" $EXPECTED_STACK_SIZE
    exit 1
fi

./test_pthread_stack_size
if [ $? -ne 0 ]; then
    echo "FAIL:" $(basename $0): "pthread stack size is not" $EXPECTED_STACK_SIZE
    exit 1
fi

echo "PASS: test user stack size"
