#!/bin/bash
# These tests validate that the kernel is build with security mitigations
# enabled or disabled to our satisfaction.

# The security specifications against which the ptest target is tested, are
# defined by "security-mitigations.txt" files which should be in this same
# directory.

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

# Print the vulnerability status to stdout.
# 1: The name of the vulnerability to check.
# This function errors if arg 1 is empty strig or the vulnerability sysfs file
# cannot be found.
function _get_mitigation_status () {
	if [ -z "$1" ]; then
		exit_error "Vulnerability name is invalid. (empty string)"
	fi

	if [ -f "${SYSFS_VULNS}/${1}" ]; then
		cat "${SYSFS_VULNS}/${1}"
	else
		exit_error "Vulnerability sysfs file \"$1\" does not exist."
	fi
}

# Compare two device code strings to one another. Return 0 if they match, else
# return 1.
# 1: device code 1; can be comma-delimited
# 2: device code 2; must be single entry
# The special code string '*' matches all.
function _match_device_code () {
	local match=false

	IFS_old=$IFS
	IFS=','
	for dev_code in $1; do
		case "$dev_code" in
			"$2")
				match=true
				;;
			\*)
				match=true
				;;
			"")
				if [ -z "$2" ]; then
					match=true
				fi
				;;
			*)
				;;
		esac
	done
	IFS=$IFS_old

	$match
}

# Parse a security mitigations rules file and pack the rules global var with
# only the rules which apply to the specified device code.
# 1: mitigations rules filepath
# 2: target device code
function _read_mitigations_rules_file () {
	# We must set noglob here bc. '*' is a valid literal character in the rules
	# file, but will be otherwise expanded annoyingly by bash.
	set -o noglob

	# for each line of in the security mitigations rules file
	while read -r line; do
		# disregard comments and blank lines
		if [[ "$line" =~ ^\ *# ]]; then
			continue
		elif [ -z "$line" ]; then
			continue
		fi

		# break the line by whitespace into words
		declare -a words="($line)"

		# iff the device code column matches this device, repack the rules
		# entry using the field seperator into the rules hashmap.
		if _match_device_code "${words[0]}" "${2}"; then
			IFS_old=$IFS
			IFS=${field_sep}
			#printf "INFO: matched line:%s\n" "${words[*]}"
			rules["${words[1]}"]="${words[*]}"
			IFS=$IFS_old
		fi
	done <"${1:-security-mitigations.txt}"
}

# TESTS #
#########

# Parses the specified security mitigations rules file and asserts the rules
# against the system state.
#
# For each valid entry, creates a new subtests and:
# 1. checks that required kernel commandline arguments are asserted (if
# applicable)
# 2. that the vulnerability status returned by the sysfs matches expectations.
#
# Fails the subtest if either of the above assertions is false.
#
# 1: security mitigations rules filepath
#    Defaults to: "security-mitigations.txt"
# 2: device code override (optional)
#    Overwrites the device code returned by the system (use for testing
#    purposes only)
function test_mitigations_from_file () {

	echo "INFO: Checking security mitigations against spec file: $1"

	device_code="`fw_printenv -n DeviceCode`" || device_code=""
	echo "INFO: device code=" "${device_code}"
	if [ -n "$2" ]; then
		device_code="${2}"
		echo "Overwriting device code as:" $device_code
	fi
	_read_mitigations_rules_file "${1:-security-mitigations.txt}" "$device_code"
	# the 'rules' variable now contains an array of rules strings which
	# apply to the current target

	declare -xi subtest_num=1
	printf "INFO: using rule: %s\n" "${rules[@]}"
	for entry in "${rules[@]}"; do
		IFS=${field_sep} read -r -a words <<<"${entry}"

		sysfs_file="${words[1]}"
		kcmdline_arg="${words[2]}"
		expected="${words[3]}"

		ptest_change_subtest $subtest_num "$sysfs_file"

		# check the kernel commandline
		if [ -z "${kcmdline_arg}" ]; then
			echo "INFO: no kernel argument for $sysfs_file, skipping cmdline check."
			ptest_pass
		elif ! _arg_in_cmdline "${kcmdline_arg}"; then
			echo "Expected kernel command argument \"${kcmdline_arg}\" not in command line."
			ptest_fail
		else
			ptest_pass
		fi

		# check the sysfs for vulnerability status
		local mitigation_status=`_get_mitigation_status ${sysfs_file}`
		if [ "${mitigation_status}" != "${expected}" ]; then
			printf "Security mitigation mismatch.\nExpected: %s\nFound: %s\n" \
				"${expected}" "${mitigation_status}"
			ptest_fail
		fi

		ptest_report
		((subtest_num++))
	done
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
	test_mitigations_from_file "security-mitigations.txt" "${1}"
fi


exit $rc
