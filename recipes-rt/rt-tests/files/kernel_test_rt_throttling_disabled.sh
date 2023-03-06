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

exit $ptest_rc
