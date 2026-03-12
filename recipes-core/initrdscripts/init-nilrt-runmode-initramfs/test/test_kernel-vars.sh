#!/bin/bash
# Unit tests for the ``kernel-vars.sh ``source script.
# This test script expects the ``kernel-vars.sh`` script to be in the same
# current working directory.


function test__debug() {
	source ./kernel-vars.sh

	local DEBUG_CMDLINES=(
		"debug"
		"foo=bar debug baz=qux"
		"rd.debug"
		"foo=bar rd.debug baz=qux"
	)
	local NODEBUG_CMDLINES=(
		"foo bar"
		"foo=bar quiet baz=qux"
	)

	for cmd in "${DEBUG_CMDLINES[@]}"; do
		CMDLINE="$cmd"
		parse_debug
		if [ "$DEBUG" != "yes" ]; then
			echo "Expected DEBUG=yes for cmdline: '$cmd', got '$DEBUG'"
			return 1
		fi
	done
	for cmd in "${NODEBUG_CMDLINES[@]}"; do
		CMDLINE="$cmd"
		parse_debug
		if [ "$DEBUG" != "" ]; then
			echo "Expected DEBUG='' for cmdline: '$cmd', got '$DEBUG'"
			return 1
		fi
	done
}


function test__rd_break() {
	source ./kernel-vars.sh

	local RD_BREAK_CMDLINES=(
		"break"
		"foo=bar break baz=qux"
		"rd.break"
		"foo=bar rd.break baz=qux"
		"break=premount"
		"foo=bar break=premount baz=qux"
	)
	local NO_RD_BREAK_CMDLINES=(
		"foo bar"
		"foo=bar quiet baz=qux"
	)

	for cmd in "${RD_BREAK_CMDLINES[@]}"; do
		CMDLINE="$cmd"
		parse_rd_break
		if [ "$RD_BREAK" != "premount" ]; then
			echo "Expected RD_BREAK=premount for cmdline: '$cmd', got '$RD_BREAK'"
			return 1
		fi
	done
	for cmd in "${NO_RD_BREAK_CMDLINES[@]}"; do
		CMDLINE="$cmd"
		parse_rd_break
		if [ "$RD_BREAK" != "" ]; then
			echo "Expected RD_BREAK='' for cmdline: '$cmd', got '$RD_BREAK'"
			return 1
		fi
	done

	# break key-values
	CMDLINE="break=premount"
	parse_rd_break
	if [ "$RD_BREAK" != "premount" ]; then
		echo "Expected RD_BREAK=premount for cmdline: '$CMDLINE', got '$RD_BREAK'"
		return 1
	fi
	CMDLINE="rd.break=postmount"
	parse_rd_break
	if [ "$RD_BREAK" != "postmount" ]; then
		echo "Expected RD_BREAK=postmount for cmdline: '$CMDLINE', got '$RD_BREAK'"
		return 1
	fi
}

function test__verbose() {
	source ./kernel-vars.sh

	local VERBOSE_CMDLINES=(
		"foo bar"
		"foo=bar baz=qux"
		"verbose"
		"foo=bar verbose baz=qux"
		"rd.info"
		"foo=bar rd.info baz=qux"
	)
	local QUIET_CMDLINES=(
		"quiet"
		"foo=bar quiet baz=qux"
	)

	for cmd in "${VERBOSE_CMDLINES[@]}"; do
		CMDLINE="$cmd"
		parse_verbose
		if [ "$VERBOSE" != "yes" ]; then
			echo "Expected VERBOSE=yes for cmdline: '$cmd', got '$VERBOSE'"
			return 1
		fi
	done
	for cmd in "${QUIET_CMDLINES[@]}"; do
		CMDLINE="$cmd"
		parse_verbose
		if [ "$VERBOSE" != "" ]; then
			echo "Expected VERBOSE='' for cmdline: '$cmd', got '$VERBOSE'"
			return 1
		fi
	done
}


function run_testcase() {
	local testcase="$1"

	echo "Running testcase: $testcase"
	$testcase
	local rc=$?
	if [ $rc -eq 0 ]; then
		echo "[PASSED]"
	else
		echo "[FAILED]"
		tc_failures=$((tc_failures + 1))
	fi
}


# ==============================================================================
# GLOBALS
# ==============================================================================

tc_failures=0


# ==============================================================================
# MAIN
# ==============================================================================

run_testcase test__debug
run_testcase test__rd_break
run_testcase test__verbose

if [ $tc_failures -eq 0 ]; then
	echo "All testcases passed!"
else
	echo "$tc_failures testcases failed."
fi
