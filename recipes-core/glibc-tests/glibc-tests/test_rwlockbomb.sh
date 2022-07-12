#!/bin/bash
# SPDX-License-Identifier: MIT
set -euo pipefail

. /usr/lib/ptest-utils/bash/ptest-format.sh

PTEST_RC_ERR=99
SCRIPT_ROOT=$(realpath $(dirname $BASH_SOURCE))
SYS_RT_THROTTLE=/proc/sys/kernel/sched_rt_runtime_us

check_system() {
	if [ $(nproc) -le 1 ]; then
		echo "ERROR: rwlockbomb test requires more than 1 processor core." >&2
		exit $PTEST_RC_ERR
	fi
}

set_cpu_throttling () {
	local throttling_us=$1
	echo "INFO: Setting sched_rd_runtime_us to ${throttling_us}" >&2
	echo $throttling_us >$SYS_RT_THROTTLE
}

test_rwlockbomb () {
	"${SCRIPT_ROOT}/rwlockbomb"
}


ptest_test=$(basename -s ".sh" "$0")
ptest_pass

check_system

read RT_THROTTLE_US_ORIG <$SYS_RT_THROTTLE
trap "set_cpu_throttling $RT_THROTTLE_US_ORIG" EXIT

set_cpu_throttling 950000
test_output=$(mktemp)

test_rwlockbomb | tee "$test_output"

if grep -q -e 'thread .* seems stuck' "$test_output"; then
	ptest_fail
fi

rm "$test_output"
ptest_report
exit $ptest_rc
