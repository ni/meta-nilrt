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

CONFIG_FILE="/etc/default/postinst"

[ -x "$CONFIG_FILE" ] || fail "$CONFIG_FILE not executable"
source "$CONFIG_FILE" || fail "Failed to run $CONFIG_FILE"

[ "$POSTINST_LOGGING" == "1" ] || fail "Invalid POSTINST_LOGGING"

# only present on first boot
if [ -e "$LOGFILE" ]; then
    if egrep -i 'warn|error|fatal|fail' "$LOGFILE"; then
        fail "One or more errors in LOGFILE=$LOGFILE"
    fi
fi

echo "PASS: $0"
trap - EXIT
