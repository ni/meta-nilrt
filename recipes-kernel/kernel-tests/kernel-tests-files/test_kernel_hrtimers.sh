#!/bin/bash

source $(dirname "$0")/ptest-format.sh
ptest_test=$(basename "$0" ".sh")

# test if hrtimers are enabled in the kernel configuration
ptest_change_subtest 1 "check hrtimers kernel configuration"
if zcat /proc/config.gz | grep -qE "^CONFIG_HIGH_RES_TIMERS=y"; then
    ptest_pass
else
    ptest_fail
fi
ptest_report

# test the current clock source configuration
machine=$(uname -m)
ptest_change_subtest 2 "test clock source configuration for $machine"
if [ "$machine" = "x86_64" ]; then
    # on x86_64 machines we expect TSC for best real-time performance
    if grep -iqs "tsc" /sys/devices/system/clocksource/clocksource0/current_clocksource; then
	ptest_pass
    else
	ptest_fail
    fi
else
    ptest_skip
fi
ptest_report

exit $ptest_rc
