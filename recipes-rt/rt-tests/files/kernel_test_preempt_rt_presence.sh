#!/bin/bash

source $(dirname "$0")/ptest-format.sh
ptest_test=$(basename "$0" ".sh")

if uname -a | grep -qs 'PREEMPT_RT'; then
	ptest_pass
else
	echo "kernel was not configured to use PREEMPT_RT"
	ptest_fail
fi

ptest_report
exit $ptest_rc
