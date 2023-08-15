#!/bin/bash

source $(dirname "$0")/ptest-format.sh

ptest_change_test $(basename "$0" ".sh") "" "Test install size against estimated max growth"

# See internal wiki for how we calculate these
X86_MAX_SIZE_EOY=1400
ARM_MAX_SIZE_EOY=300

python3 disk_size.py --x86_max_size ${X86_MAX_SIZE_EOY} --arm_max_size ${ARM_MAX_SIZE_EOY}

if [ $? -eq 0 ]; then
   ptest_pass
else
   ptest_fail
fi

ptest_report

