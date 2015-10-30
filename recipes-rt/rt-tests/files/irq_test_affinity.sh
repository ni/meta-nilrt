#!/bin/bash
#
# Test whether the smp_affinity for all running IRQ threads is CPU0
# Also test that a newly created IRQ thread has affinity set to CPU0
#

# restart active ethernet to create a new IRQ thread
restart_ethernet()
{
	# get the active network interface
	active_ethernet=$(ifconfig | grep -B 1 inet | head -n 1 | cut -d" " -f1)

	# shut down then activate the ethernet driver
	ip link set $active_ethernet down
	ip link set $active_ethernet up

	# wait for the new IRQ thread to be created
	sleep 3
}

check_irq_thread_affinity()
{
    # enumerate IRQ threads
    pids=$(grep -l irq/ /proc/*/comm | cut -d/ -f3)

    # check affinity for each IRQ thread
    affinity_fail=0
    for pid in $pids; do
	affinity=$(taskset -p $pid | cut -d: -f2)
	if [[ "$affinity" != " 1" ]]; then
	    affinity_fail=1
	    irq_cmdline=$(cat /proc/$pid/comm)
	    echo "FAIL: $irq_cmdline has affinity $affinity!"
	fi
    done

    if [ $affinity_fail -eq 1 ]; then
	exit 1
    fi
}

check_irq_thread_affinity
# restart active ethernet to create a new IRQ thread
restart_ethernet
# check affinity for running IRQ threads again
check_irq_thread_affinity

echo "PASS: irq_test_affinity"
