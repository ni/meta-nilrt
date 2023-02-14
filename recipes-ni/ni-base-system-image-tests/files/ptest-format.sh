#!/bin/bash
# Version 0.2
# Utility script containing common functions for ptest-compatible test scripts.
# USAGE:
# 1) Source this file at the top of your ptest script.
# 2) Set ptest_test to the name of your test; recommended:
#    $(basename -s ".sh" "$0")
# 3) Define your subtests in functions, which call ptest_pass when they pass and
#    ptest_fail otherwise (FAIL is DEFAULT)
# 4) Prior to calling your test function, call ptest_change_subtest
# 5) Call your test function.
# 6) Call ptest_report immediately after the function returns.

exec 2>&1  # redirect all stderr to stdout to maintain ordering of output

function ptest_report () {
	local result='FAIL'
	case $ptest_rc in
		0)
			result='PASS'
			;;
		77)
			result='SKIP'
			;;
		*)
			result='FAIL'
			;;
	esac

	if [ "$#" -gt 0 ]; then
		echo "$@"
	fi

	printf "${result}: %s" "${ptest_test}"
	if [ -n "${ptest_subtest}" ]; then
		printf " %d" "${ptest_subtest}"
		if [ -n "${ptest_subtest_desc}" ]; then
			printf " - %s" "${ptest_subtest_desc}"
		fi
	fi
	printf "\n"
}

function ptest_pass () {
	ptest_rc=0
}

function ptest_skip () {
	ptest_rc=77
}

function ptest_fail () {
	ptest_rc=1
}

# 1 - new test name
# 2 - new subtest number
# 3 - new subtest description
function ptest_change_test () {
	ptest_test="$1"
	ptest_subtest="$2"
	ptest_subtest_desc="$3"
	ptest_fail
}

# 1 - new subtest number
# 2 - new subtest description
function ptest_change_subtest () {
	ptest_subtest="$1"
	ptest_subtest_desc="$2"
	ptest_fail
}

ptest_test=''
ptest_subtest=''
ptest_subtest_desc=''
ptest_fail  # set ptest_rc to FAIL
