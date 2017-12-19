#!/bin/bash
set -e
set -u
set -o pipefail

trap 'exitCode="$?"; set +e; echo "FAIL: $0"; exit "$exitCode"' EXIT

function fail()
{
    echo "$*"
    exit 1
}

status_lines="`opkg status`"

[ "`echo "$status_lines" | wc -l`" -gt 0 ] || fail "Empty status"

if echo "$status_lines" | grep '^Status: ' | grep -v '^Status: install ok installed$'; then
    fail "One or more packages show bad status"
fi

echo "PASS: $0"
trap - EXIT
