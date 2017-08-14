#!/bin/bash
# These tests validate that depmod configuration contains no errors (such as
# not being able to find the module directory) and that lsmod reports that (at
# last some) modules are loaded. Also prints the current kernel version for
# information and debug purposes.

source $(dirname "$0")/ptest-format.sh

# passes iff depmod executes without returning errors
function test_depmod () {
	ERRORS=$(depmod --show 2>&1 1>/dev/null)
	if [ -z "$ERRORS" ]; then
		ptest_pass
	else
		echo "Depmod returned errors (below)"
		echo "$ERRORS"
		ptest_fail
	fi
}

# passes iff lsmod reports that there are modules loaded
function test_lsmod () {
	MODS=$(lsmod | tail -n +2) # lsmod w/o header line
	if [ -n "${MODS}" ]; then
		echo "Found $(echo "${MODS}" | wc -l) loaded modules."
		ptest_pass
	else
		echo "No modules are loaded."
		ptest_fail
	fi
}

# Checks $PATH_REQUIRED_MODULES for kernel modules which are requied to be
# loaded at runtime and validates that they are running. If any are not, the
# test is FAILed. If the required modules file is not present, the test is
# skipped.
PATH_REQUIRED_MODULES=$(dirname "$0")'/required_kernel_modules'
function test_required () {
	ptest_pass
	if [ ! -r "${PATH_REQUIRED_MODULES}" ]; then
		echo "REASON: required kernel modules file ${PATH_REQUIRED_MODULES} not readable."
		ptest_skip
		return
	fi

	MODS=$(lsmod | tail -n +2 | tr -s ' ' | cut -d ' ' -f 1)
	# iterate through requirement lines. Fail if a requirement is not satisfied
	while read -r line || [ -n "$line" ]; do
		local found=0
		IFS="|"; for req in $line; do
			if [ $(echo "$MODS" | grep "$req") ]; then
				echo "req=(${line}), found=(${req})"
				found=1
				break
			fi
		done

		if [ $found -eq 0 ]; then
			echo "Required module ${line} not loaded."
			ptest_fail
		fi
	done <${PATH_REQUIRED_MODULES}
	unset IFS
}

# passes iff the mod manipulation utilities are PATHable
function test_utilities () {
	ptest_pass
	local UTILITIES="depmod insmod lsmod modinfo modprobe rmmod"
	for utility in $UTILITIES; do
		if [ ! $(which $utility) ]; then
			echo "${utility} not found."
			ptest_fail
		fi
	done
}


ptest_test=$(basename "$0" ".sh")  # test_kernel_modules

# Print the kernel (name, release, version) for info and debugging
printf "INFO: %s\n" "$(uname -srv)"

ptest_change_subtest 1 "test module utilities"
test_utilities
ptest_report
rc=$ptest_rc

ptest_change_subtest 2 "depmod configuration"
test_depmod
ptest_report

ptest_change_subtest 3 "modules loaded"
test_lsmod
ptest_report

ptest_change_subtest 4 "required modules"
test_required
ptest_report

exit $rc
