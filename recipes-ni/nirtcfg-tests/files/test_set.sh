#!/bin/bash
# Exercises nirtcfg --set operations.

source $(dirname "$0")/ptest-format.sh      # for ptest_ functions.
source $(dirname "$0")/shared-functions.sh  # for nirtcfg_ functions.

ptest_test=$(basename "$0" ".sh")  # test_set


# SETUP #
#########

declare PTEST_ROOT=$(realpath $(dirname "$0"))

# SETUP TEMPDIR
if [ -z "${nirtcfg_tempdir}" ]; then
	source ./setup.sh
fi
cd "${nirtcfg_tempdir}"
mkdir "set"
cd "./set"
# END SETUP TEMPDIR


# TESTS #
#########

# Test that we can write a new section and value to a file that already exists
function test_set_normal () {
	ptest_pass

	echo "" >./var.ini

	nirtcfg_assert_pass --file ./var.ini --set section=ptest,token=foo,value=bar

	diff -a ./var.ini "${PTEST_ROOT}/configs/set_normal.correct" || ptest_fail
}

# Test that we can write a new value to a section that already exists
function test_set_section_exists () {
	ptest_pass
	echo "" >./var.ini

	nirtcfg_assert_pass --file ./var.ini --set section=ptest,token=foo,value=bar
	nirtcfg_assert_pass --file ./var.ini --set \
		section=ptest,token=fizz,value=buzz

	diff -a ./var.ini "${PTEST_ROOT}/configs/set_section-exists.correct" || \
		ptest_fail
}

# Test that we can overwrite a value that already exists
function test_set_token_exists () {
	ptest_pass
	echo "" >./var.ini

	nirtcfg_assert_pass --file ./var.ini --set section=ptest,token=foo,value=bar
	nirtcfg_assert_pass --file ./var.ini --set \
		section=ptest,token=foo,value=newvalue

	diff -a ./var.ini "${PTEST_ROOT}/configs/set_token-exists.correct" || \
		ptest_fail
}

# Test that operations which are compatible with --set are allowed to proceed.
function test_set_compatible_operations () {
	ptest_pass
	# All of the following should *PASS*
	cp "${PTEST_ROOT}/configs/base_config.ini" ./config.ini

	# set & get
	nirtcfg_assert_pass --file config.ini \
		--set section=test0,token=foo,value=bar \
		--get section=test1,token=tok1 1>/dev/null
	# set & clear
	nirtcfg_assert_pass --file config.ini \
		--set section=test0,token=foo,value=bar \
		--clear section=test1,token=tok1 1>/dev/null
	# set & another set
	nirtcfg_assert_pass --file config.ini \
		--set section=test0,token=foo,value=bar \
		--set section=test0,token=fizz,value=buzz 1>/dev/null
}

# Test that operations that are incompatible with --set are rejected.
function test_set_incompatible_operations () {
	ptest_pass
	# all of the following functions should fail
	cp "${PTEST_ROOT}/configs/base_config.ini" ./config.ini

	# set & rm-if-empty
	nirtcfg_assert_fail --file config.ini \
		--set section=test0,token=foo,value=bar --rm-if-empty
	# set & list
	nirtcfg_assert_fail --file config.ini \
		--set section=test0,token=foo,value=bar --list

	# set & migrate
	nirtcfg_assert_fail --file config.ini \
		--set section=test0,token=foo,value=bar --migrate
}

# Test that ``set``ing to a file in a directory that does not exist will result
# in the directory leading to the file being created and the backing file
# created in that directory.
function test_set_directory_dne () {
	ptest_pass

	nirtcfg_assert_pass --file ./dir_dne/newfile.ini --set \
		section=test,token=foo,value=bar

	# check permissions of new directory
	perms=$(stat -c "%a" ./dir_dne)
	if [ x"$perms" != x"755" ]; then
		echo "ERROR: Invalid permissions for created directory: $perms" >&2
		ptest_fail
	fi

	# check ownership and permissions of file
	perms=$(stat -c "%U %G %a" ./dir_dne/newfile.ini)
	if [ x"$perms" != x"lvuser ni 664" ]; then
		echo "ERROR: Invalid permissions: $perms" >&2
		ptest_fail
	fi
}

# Test that ``set``ing to a file that does not already exist results in the
# backing file being created.
function test_set_file_dne () {
	ptest_pass

	nirtcfg_assert_pass --file ./newfile.ini --set \
		section=test,token=foo,value=bar

	if [ ! -e "./newfile.ini" ]; then
		echo "ERROR: set call succeeded, but new file not created." >&2
		ptest_fail
	fi

	# check ownership and permissions
	perms=$(stat -c "%U %G %a" ./newfile.ini)
	if [ x"$perms" != x"lvuser ni 664" ]; then
		echo "Invalid permissions: $perms" >&2
		ptest_fail
	fi
}

# Read :section_chars and attempt to write the special characters in the middle
# of section names. Test the result of the operation.
function test_set_section_names () {
	echo '' >./config.ini

	ptest_pass

	while IFS='' read line; do
		IFS='x' read -ra words <<<"$line"
		local sep="${words[0]}"
		local result="${words[1]}"

		local set_string=$(printf "section=test%sing,token=foo,value=bar" \
		                          "$sep")
		local args="--file ./config.ini --set '${set_string}'"

		printf "sep=%s; expect %s\n" "$sep" "$result"
		if [ x"$result" = x"pass" ]; then
			nirtcfg_assert_pass "$args"
		else
			nirtcfg_assert_stderr "$args"
		fi
	done <"${PTEST_ROOT}/section_chars"
}

function test_set_set-string () {
	echo '' >./config.ini

	ptest_pass

	local sec="section=testing"
	local tok="token=foo"
	local val="value=bar"

	local xfail='nirtcfg_assert_stderr --file ./config.ini --set '
	local xpass='nirtcfg_assert_no_stderr --file ./config.ini --set '

	# S
	$xfail $sec
	# T
	$xfail $tok
	# V
	$xfail $val

	# ST
	$xfail $sec,$tok
	# SV
	$xfail $sec,$val
	# TS
	$xfail $tok,$sec
	# TV
	$xfail $tok,$val
	# VS
	$xfail $val,$sec
	# VT
	$xfail $val,$tok

	# SVT
	$xpass $sec,$val,$tok
	# STV
	$xpass $sec,$tok,$val
	# VST
	$xpass $val,$sec,$tok
	# VTS
	$xpass $val,$tok,$sec
	# TSV
	$xpass $tok,$sec,$val
	# TVS
	$xpass $tok,$val,$sec
}


# MAIN #
########

ptest_change_subtest 0 "compatible operations"
test_set_compatible_operations
ptest_report

ptest_change_subtest 1 "incompatible operations"
test_set_incompatible_operations
ptest_report

ptest_change_subtest 2 "normal operation"
test_set_normal
ptest_report

ptest_change_subtest 3 "file does not exist"
test_set_file_dne
ptest_report

ptest_change_subtest 4 "directory does not exist"
test_set_directory_dne
ptest_report

ptest_change_subtest 5 "section already exists"
test_set_section_exists
ptest_report

ptest_change_subtest 6 "token already exists"
test_set_token_exists
ptest_report

ptest_change_subtest 7 "section name - valid chars"
test_set_section_names
ptest_report

ptest_change_subtest 8 "set-string words"
test_set_set-string
ptest_report

exit $ptest_rc
