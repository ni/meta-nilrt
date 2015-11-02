#!/bin/sh

CACHE_SIZE_PATH='/sys/devices/system/cpu/cpu0/cache/index0/size'
NUMBER_OF_DIRECTORIES=`ls /sys/devices/system/cpu/cpu0/cache 2>/dev/null | wc -l`
EXIT_CODE=$?

if test -f $CACHE_SIZE_PATH -a $EXIT_CODE -eq 0 -a $NUMBER_OF_DIRECTORIES -gt 0
then
	echo "PASS: kernel cache_info support"
else
	echo "FAIL: kernel cache_info support"
fi
