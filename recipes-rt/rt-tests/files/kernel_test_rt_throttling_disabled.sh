#!/bin/bash

source $(dirname "$0")/ptest-format.sh
ptest_test=$(basename "$0" ".sh")

ptest_change_subtest 1 "check kernel rt throttling"
if [ $(sysctl -n kernel.sched_rt_runtime_us) -ne -1 ]; then
	ptest_fail
else
	ptest_pass
fi
ptest_report
rc_first=$ptest_rc

# now we do a jitter test to specifically verify that throttling is disabled
LOG_FILE=$(dirname "$0")'/log.txt'
ptest_change_subtest 2 "test throttling jitter"
./test_throttling_jitter
if [ $? -ne 0 ]; then
	ptest_fail
else
	ptest_pass
fi

if [ ! -r "${LOG_FILE}" ]; then
	echo "ERROR: Jitter log file ${LOG_FILE} is not readable."
else
	cat "${LOG_FILE}"
fi
ptest_report

exit $rc_first
