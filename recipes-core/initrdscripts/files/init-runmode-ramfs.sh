#!/bin/bash
set -u
set -o pipefail

# close stdin
exec 0<&-

export PATH="/sbin:/bin:/usr/sbin:/usr/bin"

umask 0022

mkdir -p /proc
mkdir -p /sys
mkdir -p /dev
mount -t proc proc /proc
mount -t sysfs sysfs /sys
mount -t devtmpfs devtmpfs /dev

# redirect stderr and stdout to /dev/kmsg
exec 2>/dev/kmsg
exec 1>&2

function status () {
    echo "initramfs: $*"
}

function warn () {
    echo "initramfs WARNING: $*"
}

# prints to kernel log and screen
function error () {
    echo "initramfs ERROR: $*"
    cat >/dev/tty0 <<ENDSCREENERROR

  initramfs ERROR: $*

ENDSCREENERROR
}

ARCH="`uname -m`"
status "Running init process on ARCH=$ARCH"

if [ "$ARCH" == "x86_64" ]; then
    status "Running depmod"
    depmod -a

    rootdevice_list="`lsblk -l -n -o NAME,PARTUUID | tr -s " " | grep " $rootuuid"`"
    rootdevice_count="`echo "$rootdevice_list" | wc -l`"
    rootdevice="/dev/`echo "$rootdevice_list" | head -1 | cut -d" " -f1`"
    case "$rootdevice_count" in
    0)  warn "rootuuid=$rootuuid not found, won't be able to boot (rootdevice=$rootdevice)" ;;
    1)  status "Found rootuuid=$rootuuid at rootdevice=$rootdevice" ;;
    \?) warn "rootuuid=$rootuuid not unique, booting first rootdevice=$rootdevice" ;;
    esac

    status "Check for tpm"
    modprobe tpm
    if [ -e "/dev/tpm0" ]; then
        if [ ! -z "${debug+x}" ]; then
            # Enable verbose status messages in nilrtdiskcrypt when
            #  kernel debug flag is passed
            export VERBOSE=1
        fi

        status "Reseal keys"
        nilrtdiskcrypt_reseal -u 0 -u 1

        status "Check for encrypted disks"
        if nilrtdiskcrypt_canopen "$rootdevice" 2>/dev/null; then
            status "Open runmode parition at rootdevice=$rootdevice"
            bootdevice="`nilrtdiskcrypt_open -k 0 -d "$rootdevice"`"

            if [ -z "$bootdevice" ]; then
                echo ""
                echo "ERROR: nilrtdiskcrypt_open failed on rootdevice=$rootdevice"
                echo "       System is unbootable. Force safemode and reformat."
                echo ""
            fi
        else
            status "No encrypted paritions found"
            rmmod tpm
            bootdevice="$rootdevice"
        fi

        # no more nilrtdiskcrypt
        unset VERBOSE
    else
        status "No /dev/tpm0"
        bootdevice="$rootdevice"
    fi

    status "Mount bootdevice=$bootdevice at /mnt/root"
    mkdir -p /mnt/root
    mount "$bootdevice" /mnt/root
    [ -f /mnt/root/sbin/init -o -L /mnt/root/sbin/init ] || error "No /mnt/root/sbin/init, cannot boot bootdevice=$bootdevice"

    status "switch_root to /mnt/root"
    exec switch_root /mnt/root /sbin/init
fi

# we should never reach this point
error "ARCH=$ARCH is not supported by this initramfs"

umount -a -r
sync

sleep 10
sync
exit 1
