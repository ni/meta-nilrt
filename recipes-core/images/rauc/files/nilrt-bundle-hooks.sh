#!/bin/bash
set -euo pipefail

# NI Linux RT image bundle install hooks
# Doc: https://rauc.readthedocs.io/en/latest/using.html#bundle-based-customization-hooks

# close stdin
exec 0<&-

# redirect stdout to stderr
exec 1<&2

function error() {
    echo "$0: ERROR: $*"
}

function info() {
    echo "$0: $*"
}

info "Run with args: $*"

case "$1" in
    install-check)
        ;;

    slot-pre-install)
        ;;

    slot-post-install)
        # XXX RAUC 1.0 doesn't provide an option to set fs labels, so we
        #  fix this up in post install. First make the device read-only
        #  then update label.

        info "Remount device=$RAUC_SLOT_DEVICE as ro,sync"
        mount -o remount,ro,sync "$RAUC_SLOT_DEVICE"
        sync

        info "Relabel device=$RAUC_SLOT_DEVICE to bootname=$RAUC_SLOT_BOOTNAME"
        fatlabel "$RAUC_SLOT_DEVICE" "$RAUC_SLOT_BOOTNAME"
        ;;

    *)
        error "$0: Invalid hook \"$1\""
        exit 1
        ;;
esac

info "SUCCESS"
exit 0

