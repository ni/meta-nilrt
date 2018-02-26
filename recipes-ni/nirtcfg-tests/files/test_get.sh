#!/bin/bash
# Exercises nirtcfg --get operations.

source $(dirname "$0")/ptest-format.sh      # for ptest_ functions.
source $(dirname "$0")/shared-functions.sh  # for nirtcfg_ functions.

ptest_test=$(basename "$0" ".sh")  # test_get


# SETUP #
#########

declare PTEST_ROOT=$(realpath $(dirname "$0"))

# SETUP TEMPDIR
if [ -z "${nirtcfg_tempdir}" ]; then
	source ./setup.sh
fi
cd "${nirtcfg_tempdir}"
mkdir "get"
cd "./get"
# END SETUP TEMPDIR


# TESTS #
#########

# Test that we can get a value that exists from a file that exists
function test_get_exists () {
	cp "${PTEST_ROOT}/configs/base_config.ini" ./config.ini
	ptest_pass

	nirtcfg_assert_value val1 --file ./config.ini --get section=test1,token=tok1
}


# Test that we can write a new value to a section that already exists
function test_get_section_exists () {
	ptest_pass
	echo "" >./var.ini

	nirtcfg_assert_pass --file ./var.ini --get section=ptest,token=foo,value=bar
	nirtcfg_assert_pass --file ./var.ini --get \
		section=ptest,token=fizz,value=buzz

	diff -a ./var.ini "${PTEST_ROOT}/configs/get_section-exists.correct" || ptest_fail
}

# Test that we get no output from a value that does not exist
function test_get_section_dne () {
	cp "${PTEST_ROOT}/configs/base_config.ini" ./config.ini
	ptest_pass

	nirtcfg_assert_no_value --file ./config.ini --get section=testdne,token=tok1
}

# Test that we can overwrite a value that already exists
function test_get_token_exists () {
	ptest_pass
	echo "" >./var.ini

	nirtcfg_assert_pass --file ./var.ini --get section=ptest,token=foo,value=bar
	nirtcfg_assert_pass --file ./var.ini --get \
		section=ptest,token=foo,value=newvalue

	diff -a ./var.ini "${PTEST_ROOT}/configs/get_token-exists.correct" || ptest_fail
}

# Test that operations which are compatible with --get are allowed to proceed.
function test_get_compatible_operations () {
	ptest_pass
	# All of the following should *PASS*
	cp "${PTEST_ROOT}/configs/base_config.ini" ./config.ini

	# get & get
	nirtcfg_assert_pass --file config.ini \
		--get section=test1,token=tok1 \
		--get section=test2,token=tok21
	# get & clear
	nirtcfg_assert_pass --file config.ini \
		--get section=test1,token=tok1 \
		--clear section=test2,token=tok21
	# get & set
	nirtcfg_assert_pass --file config.ini \
		--get section=test1,token=tok1 \
		--set section=test3,token=fizz,value=buzz
}

# Test that operations that are incompatible with --get are rejected.
function test_get_incompatible_operations () {
	ptest_pass
	# all of the following functions should fail
	cp "${PTEST_ROOT}/configs/base_config.ini" ./config.ini

	# get & rm-if-empty
	nirtcfg_assert_fail --file config.ini \
		--get section=test1,token=tok1,value=bar --rm-if-empty
	# get & list
	nirtcfg_assert_fail --file config.ini \
		--get section=test1,token=tok1,value=bar --list
	# get & migrate
	nirtcfg_assert_fail --file config.ini \
		--get section=test1,token=tok1,value=bar --migrate
}

# Test that ``get``ing a value that is not defined in the backing config file,
# while specifying a default value through ``value=`` will result in the
# default value being printed to stdout.
test_get_normal_default () {
	cp "${PTEST_ROOT}/configs/base_config.ini" ./config.ini

	ptest_pass
	nirtcfg_assert_value "default" --file ./config.ini --get \
		"section=test0,token=foo,value=default"
}

# Test that ``get``ing a value from a section that does not exist returns
# only a newline character.
test_get_section_dne () {
	cat <<EOF >./config.ini
[testsection]
tok1=val1

EOF

	ptest_pass
	nirtcfg_assert_no_value --file ./config.ini --get \
		section=wrongsection,token=tok1
}

# Test that ``get``ing a value from a token that does not exist returns
# only a newline character.
test_get_token_dne () {
	cat <<EOF >./config.ini
[testsection]
tok1=val1

EOF

	ptest_pass
	nirtcfg_assert_no_value --file ./config.ini --get \
		section=testsection,token=tok2
}

# Test that ``get`ing a value from a file that does not exist returns an empty
# stdout, but not an error.
test_get_file_dne () {
	ptest_pass
	nirtcfg_assert_no_value --file ./does_not_exist.ini --get \
		section=test1,token=tok1
}

# Test that ``get`ing a value from a file that does not exist while specifying
# a default returns the default value, but not an error.
test_get_file_dne_default () {
	ptest_pass
	nirtcfg_assert_value "default" --file ./does_not_exist.ini --get \
		section=test1,token=tok1,value=default
}


function test_get_get-string () {
	cp "${PTEST_ROOT}/configs/base_config.ini" ./config.ini

	ptest_pass

	local sec="section=test1"
	local tok="token=tok1"
	local val="value=bar"

	local xfail='nirtcfg_assert_stderr --file ./config.ini --get '
	local xpass='nirtcfg_assert_value val1 --file ./config.ini --get '

	# S
	$xfail $sec
	# T
	$xfail $tok
	# V
	$xfail $val

	# ST
	$xpass $sec,$tok
	# SV
	$xfail $sec,$val
	# TS
	$xpass $tok,$sec
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
test_get_compatible_operations
ptest_report

ptest_change_subtest 1 "incompatible operations"
test_get_incompatible_operations
ptest_report

ptest_change_subtest 2 "value exists"
test_get_exists
ptest_report

ptest_change_subtest 3 "section does not exist"
test_get_section_dne
ptest_report

ptest_change_subtest 4 "token does not exist"
test_get_token_dne
ptest_report

ptest_change_subtest 5 "default value"
test_get_normal_default
ptest_report

ptest_change_subtest 6 "file does not exist"
test_get_file_dne
ptest_report

ptest_change_subtest 7 "file does not exist +default"
test_get_file_dne_default
ptest_report

ptest_change_subtest 7 "get-string words"
test_get_get-string
ptest_report

exit $ptest_rc
