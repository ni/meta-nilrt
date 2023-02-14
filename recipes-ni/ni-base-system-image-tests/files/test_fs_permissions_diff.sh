#!/bin/bash

source $(dirname "$0")/ptest-format.sh

ptest_change_test $(basename "$0" ".sh") "" "Diff filesystem permissions with previous"

source /home/admin/.mongodb.creds
python3 fs_permissions_diff.py --server $MONGO_SERVER --user $MONGO_USER --password $MONGO_PASSWORD

if [ $? -eq 0 ]; then
   ptest_pass
else
   ptest_fail
fi

ptest_report
