#!/bin/sh
cd /ptests
source ./run-cyclictest
TEST_LOG=${LOG_DIR}/run_cyclictest-${1}.log
run_cyclictest "${1}" > ${TEST_LOG} 2>&1
echo "${CYCLICTEST_RESULT}"

