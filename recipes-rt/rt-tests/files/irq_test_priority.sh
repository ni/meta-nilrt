#!/bin/bash
# This function checks whether the the priority of the eth0 irq thread can be
# changed via /proc/irq/.../priority

source $(dirname "$0")/ptest-format.sh

function get_prio_for_task () {
	#RT_priority is the 40th field in /proc/[PID]/stat
	awk '{print $40;}' "/proc/$1/stat"
}

function test_priority() {
	local PING_DEST="8.8.8.8"  # Google's public DNS server
	ptest_pass

	# Find the IRQ for active network interface and increment the existing priority
	[[ $(ip route get $PING_DEST) =~ dev\ ([^ ]+) ]] && active_network_if=${BASH_REMATCH[1]}

	dev_irqs=`{ grep "${active_network_if}-rx" /proc/interrupts || grep "${active_network_if}" /proc/interrupts | head -n 1; } | cut -d: -f1`

	declare -A irq_pids
	declare -A new_prios

	for dev_irq in $dev_irqs; do
		if [ ! -e "/proc/irq/$dev_irq/priority" ]; then
			echo "ERROR (fatal): /proc/irq/$dev_irq/priority does not exist"
			ptest_fail
		fi

		irq_pids[$dev_irq]=`ps -o pid,comm | grep "irq\/$dev_irq" | awk -F' ' '{print $1}'`

		(( dev_prio=$(get_prio_for_task ${irq_pids[$dev_irq]}) ))
		let dev_prio++
		new_prios[$dev_irq]=$dev_prio

		echo "$dev_prio" > "/proc/irq/$dev_irq/priority"
	done
	if [ $ptest_rc -gt 0 ]; then
		return
	fi

	# Force some network activity (priority is updated when the IRQ handler runs)
	ping -c 1 $PING_DEST >/dev/null 2>&1

	# Check if IRQ priority increased
	let priority_increased=0
	for dev_irq in $dev_irqs; do
		if [ ${new_prios[$dev_irq]} -eq `awk '{print $40}' /proc/${irq_pids[$dev_irq]}/stat` ]; then
			let priority_increased=1
			break
		fi
	done

	if [ $priority_increased -eq 0 ]; then
		echo "ERROR (fatal): IRQ priority did not increase"
		ptest_fail
	fi

	# Check if IRQ priority is preserved on irq release/request (i.e. when new irq thread is created)
	ifconfig $active_network_if down; ifconfig $active_network_if up;

	let priority_preserved=0
	for dev_irq in $dev_irqs; do
		irq_pid=`ps -o pid,comm | grep "irq\/$dev_irq" | awk -F' ' '{print $1}'`
		if [ ${new_prios[$dev_irq]} -eq `awk '{print $40}' /proc/$irq_pid/stat` ]; then
			let priority_preserved=1
		fi
	done

	if [ $priority_preserved -eq 0 ]; then
		echo "ERROR (fatal): IRQ priority was not preserved on irq release/request"
		ptest_fail
	fi

	#Cleanup
	for dev_irq in $dev_irqs; do
		let old_prio=${new_prios[$dev_irq]}-1
		echo "$old_prio" > "/proc/irq/$dev_irq/priority"
	done
}

ptest_test=$(basename "$0" ".sh")
test_priority
ptest_report

exit $ptest_rc
