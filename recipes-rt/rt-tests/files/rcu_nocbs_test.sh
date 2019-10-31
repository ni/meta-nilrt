#!/bin/bash
# Verifies "rcu_nocbs" bitmask (Linux boot arg) contains every CPU.
set -euo pipefail
readonly SCRIPT_NAME="rcu_nocbs_test"

function cleanup () {
    exitCode="$?"

    set +e

    if [ "$exitCode" == "0" ]; then
        echo "PASS: $SCRIPT_NAME"
    else
        echo "FAIL: $SCRIPT_NAME"
    fi

    exit "$exitCode"
}

trap cleanup EXIT

function error() {
    local msg="$*"
    echo 1>&2 "ERROR: $msg"
    false
}

rcu_nocbs_line=$(cat /proc/cmdline | tr ' ' '\n' | grep '^rcu_nocbs=') || error "rcu_nocbs missing on /proc/cmdline"
[ "$rcu_nocbs_line" == "rcu_nocbs=all" ] || error "Found '$rcu_nocbs_line' on /proc/cmdline, expecting 'rcu_nocbs=all'"

ps -ef | egrep '\[rcuo[a-zA-Z0-9]+/[0-9]+\]' >/dev/null || error "Did not find any 'rcuo' kthread"

