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

ORIG_KMSG_CONFIG="`cat /proc/sys/kernel/printk_devkmsg`"
echo on              >"/proc/sys/kernel/printk_devkmsg"
status "Set printk_devkmsg=on (previous: $ORIG_KMSG_CONFIG)"

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

    status "Check for TPM"
    modprobe tpm_tis
    if [ -e "/dev/tpm0" ]; then
        # Enable verbose status messages in nilrtdiskcrypt when
        #  "initramfs_debug" or "debug" flag are passed to kernel
        if egrep "(^| )initramfs_debug($| )" /proc/cmdline; then
            export VERBOSE=2
        elif egrep "(^| )debug($| )" /proc/cmdline; then
            export VERBOSE=1
        fi

        status "Reseal keys"
        nilrtdiskcrypt_reseal -u 0 -u 1

        status "Check for encrypted disks"
        if nilrtdiskcrypt_canopen -d "$rootdevice"; then
            status "Open runmode parition at rootdevice=$rootdevice"
            bootdevice="`nilrtdiskcrypt_open -k 1 -d "$rootdevice"`"

            if [ -z "$bootdevice" ]; then
                error \
                    "nilrtdiskcrypt_open failed on rootdevice=$rootdevice" \
                    "System is unbootable. Force safemode and reformat."
            fi
        else
            status "No encrypted paritions found"
            bootdevice="$rootdevice"

            status "Cleanup TPM modules"
            modprobe -r tpm_tis
            remaining_modules="`lsmod | grep tpm || true`"
            [ -z "$remaining_modules" ] || warn "TPM modules remaining after cleanup: $remaining_modules"
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

    if [ -f /mnt/root/sbin/init -o -L /mnt/root/sbin/init ]; then
        status "switch_root to /mnt/root (restore printk_devkmsg=$ORIG_KMSG_CONFIG)"
        echo "$ORIG_KMSG_CONFIG" >"/proc/sys/kernel/printk_devkmsg"
        exec switch_root /mnt/root /sbin/init
    else
        error "No /mnt/root/sbin/init, cannot boot bootdevice=$bootdevice"
    fi
else
    error "ARCH=$ARCH is not supported by this initramfs"
fi

# cleanup
umount -a -r
sync

sleep 10
sync
exit 1
