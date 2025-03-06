#!/bin/bash

source $(dirname "$0")/ptest-format.sh

ptest_change_test $(basename "$0" ".sh") "" "Diff dmesg log with with previous"

source /home/admin/.mongodb.creds
if [ -e /home/admin/.test.kernel_dmesg_diff.args ]; then
   source /home/admin/.test.kernel_dmesg_diff.args
fi

python3 kernel_dmesg_diff.py --server $MONGO_SERVER --user $MONGO_USER --password $MONGO_PASSWORD "${KERNEL_DMESG_DIFF_TEST_EXTRA_ARGS[@]}"

if [ $? -eq 0 ]; then
   ptest_pass
else
   ptest_fail
fi

ptest_report
