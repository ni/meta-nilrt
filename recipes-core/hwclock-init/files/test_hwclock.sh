#! /bin/bash

# Test the hwclock functionality needed by /etc/init.d/hwclock.sh
# Namely: --hctosys, --systohc, & --show

# Note that this script is destructive with respect to the state of the system
# clock and rtc!

source ./ptest-format.sh

ptest_test=$(basename "$0" .sh)  # test_hwclock


# SETUP TEMPDIR #
#################

declare -r HWCLOCK_TEMPDIR=$(mktemp -d)
if [ -z "$HWCLOCK_TEMPDIR" ]; then
	echo "ERROR: could not create tempdir" >&2 && \
	exit 1
fi

cd $HWCLOCK_TEMPDIR

# MISC FUNCTIONS #
##################

function hwclock_debug_output () {
	printf "<args>%s</args>\n" "$hwclock_args" >&2
	printf "<return_code>%d</return_code>\n" $hwclock_rc >&2
	printf "<stdout>%s</stdout>\n" "$hwclock_stdout" >&2
	printf "<stderr>%s</stderr>\n" "$hwclock_stderr" >&2
}

function hwclock_store_output () {
	hwclock_args="$@"
	hwclock $@ 1>./hwclock_stdout 2>./hwclock_stderr

	hwclock_rc=$?
	hwclock_stdout=$(cat ./hwclock_stdout)
	hwclock_stderr=$(cat ./hwclock_stderr)
}

function which_hwclock () {
	skip_tests=false

	which_hwclock=$(which hwclock)
	if [ -z "$which_hwclock" ]; then
		echo "ERROR: no hwclock implementaton present." >&2
		return
	else
		printf "INFO: %s\n" "$(stat -c %N $which_hwclock)"
	fi

	echo "INFO:" $(hwclock --version)

	output=$(hwclock --show 2>&1)
	if [ $? -ne 0 ]; then
		echo "INFO: No RTC detected, skipping tests."
		skip_tests=true
	fi
}

# SUBTESTS #
############

# Test that the hwclock can set the system clock
# Expect return code 0, no stderr, and no stderr
function test_hwclock_hctosys () {
	ptest_pass
	hwclock_store_output --hctosys

	if [ $hwclock_rc -ne 0 -o \
	     -n "$hwclock_stderr" -o \
	     -n "$hwclock_stdout" ]; then
		echo "ERROR: hwclock --hctosys returned error." >&2
		hwclock_debug_output
		ptest_fail
	fi
}

# Test that hwclock_show returns code 0, no stderr, and some stdout
function test_hwclock_show () {
	ptest_pass
	hwclock_store_output --show

	if [ $hwclock_rc -ne 0 -o \
	     -n "$hwclock_stderr" -o \
	     -z "$hwclock_stdout" ]; then
		echo "ERROR: hwclock --show returned error." >&2
		hwclock_debug_output
		ptest_fail
	fi
}

# Test that the systemclock can set the hardware clock
# Expect return code 0, no stderr, and no stderr
function test_hwclock_systohc () {
	ptest_pass
	hwclock_store_output --systohc

	if [ $hwclock_rc -ne 0 -o \
	     -n "$hwclock_stderr" -o \
	     -n "$hwclock_stdout" ]; then
		echo "ERROR: hwclock --systohc returned error." >&2
		hwclock_debug_output
		ptest_fail
	fi
}

# MAIN #
########

which_hwclock

# --show
ptest_change_subtest 1 "operation: show"
if ! $skip_tests; then
	test_hwclock_show
else
	ptest_skip
fi
ptest_report

# --hctosys
ptest_change_subtest 2 "operation: hctosys"
if ! $skip_tests; then
	test_hwclock_hctosys
else
	ptest_skip
fi
ptest_report

# --systohc
ptest_change_subtest 3 "operation: systohc"
if ! $skip_tests; then
	test_hwclock_systohc
else
	ptest_skip
fi
ptest_report

# TEARDOWN #
rm -r $HWCLOCK_TEMPDIR

exit $ptest_rc
