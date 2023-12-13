#!/bin/sh
cd /ptests
source run-cyclictest
mkdir -p /var/cache/fio
fio fio.cfg --ioengine="sync" --runtime="${TEST_DURATION}" --ramp_time=1m \
    --output="${LOG_DIR}/fio-sync-`date +'%Y_%m_%d-%H_%M_%S'`.log" \
    > /dev/null

