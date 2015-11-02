#!/bin/bash

SWAP_SIZE=$(cat /proc/meminfo | grep -i Swap | awk '{sum+=$2} END {print sum}')
if [ $SWAP_SIZE -ne 0 ]; then
    echo "FAIL:" $(basename $0): "Swap space is in use"
    exit 1
fi

NB_SWAP_PARTS=$(cat /proc/swaps | wc -l)
if [ $NB_SWAP_PARTS -ne 1 ]; then
    echo "FAIL:" $(basename $0): "Swap partitions are in use (visit /proc/swap for more info)"
    exit 1
fi

# this is just an additional check to make sure it's disabled
SWAP_SIZE=$(free | grep Swap | awk '{print $2+$3+$4}')
if [ $SWAP_SIZE -ne 0 ]; then
    echo "FAIL:" $(basename $0): "Swap space is in use"
    exit 1
fi

echo "PASS: swap memory is disabled"

