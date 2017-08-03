#!/bin/bash
#
# Test whether the smp_affinity for all running IRQ threads is CPU0
# Also test that a newly created IRQ thread has affinity set to CPU0
#

source $(dirname "$0")/ptest-format.sh

ptest_test=$(basename "$0" ".sh")

# restart active ethernet to create a new IRQ thread
restart_ethernet()
{
	# Get the active network interface.
	# This section can fail if the ethernet link is in a DOWN state; which
	# it frequently is (temporarily), if this test is run after
	# irq_test_priority. So we try twice with a 5 second gap.
	active_ethernet=$(ip link | grep "state UP" | head -1 | cut -d":" -f2)

	if [ -z "${active_ethernet}" ]; then
		echo "ERROR: First attempt at finding an UP ethernet link failed."
		ip link
		echo "Retrying in 5 seconds..."
		sleep 5
		active_ethernet=$(ip link | grep "state UP" | head -1 | cut -d":" -f2)
		if [ -z "${active_ethernet}" ]; then
			echo "ERROR: Second attempt failed."
			ip link
			echo "Failing restart_ethernet()"
			ptest_fail
			return 1
		else
			echo "Second attempt suceeded."
		fi
	fi

	# shut down then activate the ethernet driver
	ip link set $active_ethernet down
	ip link set $active_ethernet up

	# wait for the new IRQ thread to be created
	sleep 3
	return 0
}

check_irq_thread_affinity()
{
	# enumerate IRQ threads
	pids=$(grep -l irq/ /proc/*/comm | cut -d/ -f3)

	# check affinity for each IRQ thread
	ptest_pass
	for pid in $pids; do
		affinity=$(taskset -p $pid | cut -d: -f2)
		if [[ "$affinity" != " 1" ]]; then
			ptest_fail
			irq_cmdline=$(cat /proc/$pid/comm)
			echo "ERROR: $irq_cmdline has affinity $affinity! Expected: 1"
		fi
	done
}

ptest_change_subtest 1 "check affinity"
check_irq_thread_affinity
ptest_report
first_rc=$ptest_rc

ptest_change_subtest 2 "check affinity after restart"
if [ $first_rc -gt 0 ]; then
	echo "REASON: No need to check restart if subtest 1 fails."
	ptest_skip
else
	# restart active ethernet to create a new IRQ thread
	if ! restart_ethernet; then
		ptest_fail
	else
		check_irq_thread_affinity
	fi
fi
ptest_report

exit $first_rc
