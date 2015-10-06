: ${nisetcommitratio:=/etc/init.d/nisetcommitratio}
: ${tmpdir:=/tmp/test-nisetcommitratio}
: ${tmp_memreserve_dir:="$tmpdir/memreserve-test.d"}
: ${tmp_overcommit_ratio:="$tmpdir/overcommit_ratio"}
: ${stderr:=$tmpdir/stderr.txt}

rm -rf "$tmpdir"
mkdir -p "$tmpdir"

declare -g FAILED=0
fail () {
	verbose "failed on line ${BASH_LINENO[0]}"
	FAILED=1
}

verbose () {
	(( VERBOSE )) && echo "$@" >&2
}

record_test_result () {
	local result="$1" test="$2"
	if [[ -v REMAINING_TESTS["$test"] ]]; then
		echo "$result: $test"
		unset REMAINING_TESTS["$test"]
	else
		verbose "Invalid test: '$test'"
		echo "FAIL: $test"
	fi
}

onexit () {
	for t in "${REMAINING_TESTS[@]:+${REMAINING_TESTS[@]}}"; do
		verbose "Test not completed on exit: '$test'"
		record_test_result FAIL "$t"
	done
}

trap onexit EXIT

declare -g CURRENT_TEST=

test_start () {
	if [[ -n $CURRENT_TEST ]]; then
		verbose "Test not completed before start of next test: '$test'"
		record_test_result FAIL "$CURRENT_TEST"
	fi
	CURRENT_TEST="$1"
	rm -rf "$tmp_memreserve_dir"
	mkdir -p "$tmp_memreserve_dir"
	rm -f "$stderr"
	FAILED=0
}

test_finish () {
	local test="$1"
	if [[ -z $CURRENT_TEST ]]; then
		verbose "Test completed but not started: '$test'"
		record_test_result FAIL "$test"
	elif [[ $CURRENT_TEST != $test ]]; then
		verbose "Test '$test' completed while a different test ('$CURRENT_TEST') started"
		record_test_result FAIL "$CURRENT_TEST"
	fi
	rm -f $tmp_overcommit_ratio
	rm -f $stderr
	if (( FAILED )); then
		record_test_result FAIL "$CURRENT_TEST"
	else
		record_test_result PASS "$CURRENT_TEST"
	fi
	FAILED=0
	CURRENT_TEST=
}
