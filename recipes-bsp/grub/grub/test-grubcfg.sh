#! /bin/bash

# Test that expected kernel cmdline parameters are being passed from grub.

# This ptest is intended to be run by NI's internal ATS systems.

source $(dirname "$0")/ptest-format.sh

function test_default_grubcfg () {
	ptest_pass

	if ! grep -q "rootwait rw usbcore.usbfs_memory_mb=0 consoleblank=0 rcu_nocbs=all mitigations=off quiet console=tty0 console=ttyS0,115200n8 sys_reset=false" /home/admin/cmdline_default.out; then
		echo "ERROR: unexpected arguments on default kernel cmdline" >&2
		ptest_fail
	fi
}

function test_mitigation_on_grubcfg () {
	ptest_pass

	if ! grep -q "rootwait rw usbcore.usbfs_memory_mb=0 consoleblank=0 rcu_nocbs=all quiet console=tty0 console=ttyS0,115200n8 sys_reset=false" /home/admin/cmdline_mitigations_on.out; then
		echo "ERROR: unexpected arguments on kernel cmdline with mitigations on" >&2
		ptest_fail
	fi
}

ptest_change_subtest 1 "test_default_grubcfg"
test_default_grubcfg
ptest_report

ptest_change_subtest 2 "test_mitigation_on_grubcfg"
test_mitigation_on_grubcfg
ptest_report

exit $ptest_rc
