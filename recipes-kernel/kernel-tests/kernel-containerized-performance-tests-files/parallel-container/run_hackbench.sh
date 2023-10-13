#!/bin/sh
cd /ptests
source run-cyclictest
hackbench -l 36000000 -g 10 \
    2>/dev/null > "${LOG_DIR}/hackbench-`date +'%Y_%m_%d-%H_%M_%S\'`.log"

