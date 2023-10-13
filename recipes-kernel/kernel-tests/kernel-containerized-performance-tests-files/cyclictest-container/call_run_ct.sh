#!/bin/sh
cd /ptests
source ./run-cyclictest
TEST_LOG=${LOG_DIR}/run_cyclictest-`date +'%Y_%m_%d-%H_%M_%S'`-${1}.log
run_cyclictest "${1}" > ${TEST_LOG} 2>&1
echo "${CYCLICTEST_RESULT}"

