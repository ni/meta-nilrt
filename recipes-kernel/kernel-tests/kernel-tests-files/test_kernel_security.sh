#!/bin/bash
# These tests validate that the kernel is build with security mitigations
# enabled or disabled to our satisfaction.

source $(dirname "$0")/ptest-format.sh

declare -xr SYSFS_VULNS="/sys/devices/system/cpu/vulnerabilities"

# rules hashmap of ['vuln_name'] = 'rules_entry'
declare -A rules

# field seperator for delimiting rules words; this character is not allowed in
# the security mitigations rules files.
declare -r field_sep=$'|'

function exit_error () {
	echo "$1"
	exit 99
}

# Grep the kernel commandline for the presence of a provided argument.
# 1: Commandline argument to search for
function _arg_in_cmdline () {
	grep -q -E "(^| +)$1( +|$)" /proc/cmdline
}

# Check the test machine architecture. If it is a supported arch for this test,
# return 0; otherwise, return 1.
function _check_machine_arch () {
	case `uname -m` in
		x86_64)
			return 0
			;;
		*)
			return 1
			;;
	esac
}

function report_cpu () {
	echo "INFO: cpu under test:"
	while read -r line; do
		case "${line}" in
			"vendor_id"*|"cpu family"*|"model"*|"stepping"*|"microcode"*|"bugs"*)
				echo "INFO:     $(echo "${line}" | sed -r 's/(\s*):(\s*)/: /;')"
		;;
		esac
		# break on first blank line: we only want to print processor 0
		# because we assume CPU cores to be homogeneous (at least to
		# the extent of having the same bugs)
		if [ "${line}" = "" ]; then
			break
		fi
	done </proc/cpuinfo
}

function report_vulnerability_status () {
	echo "INFO: vulnerability status:"
	for id in $(ls ${SYSFS_VULNS}); do
		status=$(cat ${SYSFS_VULNS}/${id})
		echo "INFO:     ${id}: ${status}"
	done
}

function test_mitigations () {
	report_cpu
	report_vulnerability_status

	echo "INFO: kernel cmdline:"
	echo "INFO:     $(cat /proc/cmdline)"

	kcmdline_arg="mitigations=off"
	if ! _arg_in_cmdline "${kcmdline_arg}"; then
		echo "Expected kernel command argument \"${kcmdline_arg}\" not in command line."
		ptest_fail
	else
		ptest_pass
	fi

	ptest_report
}

# MAIN #
########

ptest_test=$(basename "$0" ".sh")

rc=$ptest_rc

if ! _check_machine_arch; then
	echo "INFO: machine architecture not supported for this test"
	ptest_skip
	ptest_report
else
	test_mitigations
fi

exit $rc
