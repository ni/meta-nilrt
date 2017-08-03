#!/bin/bash

source $(dirname "$0")/ptest-format.sh
ptest_test=$(basename "$0" ".sh")

if [ $(uname -a | grep 'PREEMPT RT' | wc -l) -eq 0 ]; then
	echo "kernel was not configured to use PREEMPT_RT"
	ptest_fail
else
	ptest_pass
fi

ptest_report
exit $ptest_rc
