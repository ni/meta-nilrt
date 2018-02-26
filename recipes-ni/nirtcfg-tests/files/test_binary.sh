#!/bin/bash

source $(dirname "$0")/ptest-format.sh

ptest_test=$(basename "$0" ".sh")  # test_binary


# SETUP #
#########

declare nirtcfg_path=$(which nirtcfg)


# TESTS #
#########

# Test that nirtcfg is accessible in the currently configured PATH.
# If this test fails, the rest of the tests in the nirtcfg ptest suite will
# also fail (or be skipped).
function test_binary_in_path () {
	nirtcfg --help 1>/dev/null && ptest_pass
}

# Test that the nirtcfg binary is in the correct location on disk.
function test_binary_path_correct () {
	nirtcfg_path=$(which nirtcfg)
	if [ x"$nirtcfg_path" != x"/usr/local/natinst/bin/nirtcfg" ]; then
		echo "Invalid nirtcfg path: ${nirtcfg_path}" >&2
		ptest_fail
	else
		ptest_pass
	fi
}

# Test that the nirtcfg binary has the correct ownership information.
function test_binary_ownership () {
	if [ -z "${nirtcfg_path}" ]; then
		echo "ERROR: nirtcfg path is null." >&2
		ptest_fail
		return
	fi

	declare -i CORRECT_USER=0
	declare CORRECT_GROUP=ni
	declare -i nirtcfg_user
	declare nirtcfg_group

	ptest_pass  # set here to be challenged in the following operations

	# Check owner user by id (should be 0/root)
	nirtcfg_user=$(stat -c %u "${nirtcfg_path}")
	if [ $nirtcfg_user -ne $CORRECT_USER ]; then
		echo "Incorrect owner user: id=${nirtcfg_user}" >&2
		ptest_fail
	fi

	# Check owner group by name (should be ni)
	nirtcfg_group=$(stat -c %G "${nirtcfg_path}")
	if [ x"$nirtcfg_group" != x"$CORRECT_GROUP" ]; then
		echo "Incorrect owner group: group=${nirtcfg_group}" >&2
		ptest_fail
	fi
}

# Test that the nirtcfg binary has the correct permissions.
function test_binary_perms () {
	declare CORRECT_PERMS="550"
	ptest_pass

	if [ -z "${nirtcfg_path}" ]; then
		echo "ERROR: nirtcfg path is null." >&2
		ptest_fail
		return
	fi

	# Check owner user by id (should be 0/root)
	declare nirtcfg_perms=$(stat -c %a "${nirtcfg_path}")
	if [ x"$nirtcfg_perms" != x"$CORRECT_PERMS" ]; then
		echo "Incorrect binary perms: perms=${nirtcfg_perms}" >&2
		ptest_fail
	fi
}


# MAIN #
########

ptest_change_subtest 0 "binary in PATH"
test_binary_in_path
ptest_report

ptest_change_subtest 1 "path"
test_binary_path_correct
ptest_report

ptest_change_subtest 2 "ownership"
test_binary_ownership
ptest_report

ptest_change_subtest 3 "permissions"
test_binary_perms
ptest_report

exit $ptest_rc
