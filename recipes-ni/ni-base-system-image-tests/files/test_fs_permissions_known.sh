#!/bin/bash

source $(dirname "$0")/ptest-format.sh

ptest_change_test $(basename "$0" ".sh") "" "Check known filesystem permissions"

python3 fs_permissions_known.py

if [ $? -eq 0 ]; then
   ptest_pass
else
   ptest_fail
fi

ptest_report
