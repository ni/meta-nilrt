#!/bin/bash

# $1: name of task
# $2: policy it should have
# $3: priority it should have

check_prio_for_task()
{
	tokens=( `ps -o pid,comm | grep " $1"` )
	pid=${tokens[0]}

	policy_line=`chrt -p $pid | grep policy`
	regex=".*: SCHED_(.*)"
	[[ $policy_line =~ $regex ]]
	policy=${BASH_REMATCH[1]}

	if [ $policy != $2 ]; then
		echo "FAIL: task $1 expected policy $2, but got policy $policy"
		exit -2
	fi

	prio_line=`chrt -p $pid | grep priority`
	regex=".*: (.*)"
	[[ $prio_line =~ $regex ]]
	prio=${BASH_REMATCH[1]}

	if [ $prio != $3 ]; then
		echo "FAIL: task $1 expected priority $3, but got priority $prio"
		exit -2
	fi
}

check_prio_for_task kthreadd FIFO 25
check_prio_for_task ksoftirqd/0 FIFO 8
check_prio_for_task irq FIFO 15

echo "PASS: kthread_test_priority"
