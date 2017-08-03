#!/bin/bash

source $(dirname "$0")/ptest-format.sh
ptest_test=$(basename "$0" ".sh")

# $1: name of task
# $2: policy it should have
# $3: priority it should have
function check_prio_for_task() {
	ptest_pass

	tokens=( `ps -e -o pid,comm | grep " $1"` )
	pid=${tokens[0]}

	policy_line=`chrt -p $pid | grep policy`
	regex=".*: SCHED_(.*)"
	[[ $policy_line =~ $regex ]]
	policy=${BASH_REMATCH[1]}

	if [ $policy != $2 ]; then
		echo "task $1 expected policy $2, but got policy $policy" >&2
		ptest_fail
	fi

	prio_line=`chrt -p $pid | grep priority`
	regex=".*: (.*)"
	[[ $prio_line =~ $regex ]]
	prio=${BASH_REMATCH[1]}

	if [ $prio != $3 ]; then
		echo "task $1 expected priority $3, but got priority $prio" >&2
		ptest_fail
	fi
}

ptest_change_subtest 1 kthreadd
check_prio_for_task kthreadd FIFO 25
ptest_report
rc_first=$ptest_rc

ptest_change_subtest 2 ksoftirqd
check_prio_for_task ksoftirqd/0 FIFO 8
ptest_report

ptest_change_subtest 3 irq
check_prio_for_task irq FIFO 15
ptest_report

exit $rc_first
