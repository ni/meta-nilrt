#!/bin/bash
set -euo pipefail

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
    echo "initramfs: $*" || true
}

function warn () {
    echo "initramfs WARNING: $*" || true
}

# prints to kernel log and screen
function error () {
    echo "initramfs ERROR: $*" || true

    cat >/dev/tty0 || true <<ENDSCREENERROR

  initramfs ERROR: $*

ENDSCREENERROR

    false
}

function panic_and_reboot() {
    set +e

    error "Uh oh. Something went wrong. Rebooting in 10 seconds."

    umount -a -r
    sync

    sleep 10
    reboot -f

    exit 1
}

# This script never exits on a successful run
trap panic_and_reboot EXIT

ORIG_KMSG_CONFIG=$(cat "/proc/sys/kernel/printk_devkmsg")
echo on              > "/proc/sys/kernel/printk_devkmsg"
status "Set printk_devkmsg=on (previous: $ORIG_KMSG_CONFIG)"

ARCH=$(uname -m)
status "Running init process on ARCH=$ARCH"

# HACK: BIOS enables cstates when they should be disabled and this makes the
# processor frequency go bonkers on crio's 903x dual-core (not 9034) affecting
# performance of the restore-mode's rootfs unpacking. We disable all cstates
# except C0 in all cpu cores until we get a BIOS update with cstates disabled
if [ "$ARCH" == "x86_64" ]; then
(
    shopt -s nullglob
    for cstate_disable_file in /sys/devices/system/cpu/cpu*/cpuidle/state[^0]/disable; do
        status "Setting cstate_disable_file=$cstate_disable_file"
        echo 1 > "$cstate_disable_file"
    done
)
fi

# Asserts grep-safe fs identifiers
function assert_valid_fs_id() {
    local name="$1"
    local value="$2"
    echo "$value" | egrep -q '^[a-zA-Z0-9\-]+$' || error "$name: Invalid fs label or UUID"
}

# Parse kernel cmd line to find nibootX partition's fs label
niboot_fs_label=$(cat /proc/cmdline | tr " " "\n" | grep "^rauc.slot=" | head -1 | cut -d= -f2)
niboot_fs_uuid=$(cat /proc/cmdline | tr " " "\n" | grep "^rauc.slot.uuid=" | head -1 | cut -d= -f2)

assert_valid_fs_id "rauc.slot" "$niboot_fs_label"
assert_valid_fs_id "rauc.slot.uuid" "$niboot_fs_uuid"

# Find boot dev nodes using a combination of fs label and UUID so that
#  multiple NILRT installations may co-exist on the same system
niboot_part_device=$(lsblk -l -n -o NAME,LABEL,UUID | tr -s " " | egrep " $niboot_fs_label $niboot_fs_uuid\$" | head -1 | cut -d" " -f1)
boot_disk_device=$(lsblk -l -n -o PKNAME "/dev/$niboot_part_device")

assert_valid_fs_id "boot_disk_device" "$boot_disk_device"

# Find niuser partition dev on same disk that we booted
niuser_part_device=$(lsblk -l -n -o NAME,PKNAME,PARTLABEL | tr -s " " | egrep " $boot_disk_device niuser\$" | head -1 | cut -d" " -f1)

status "Booting niboot_fs_label=$niboot_fs_label, niboot_fs_uuid=$niboot_fs_uuid, boot_disk_device=$boot_disk_device, niboot_part_device=$niboot_part_device, niuser_part_device=$niuser_part_device"

# Mount nibootX filesystem
mkdir -p /mnt/niboot 
mount -o ro,sync,relatime "/dev/$niboot_part_device" /mnt/niboot

mkdir -p /mnt/niuser

if ! mount "/dev/$niuser_part_device" /mnt/niuser; then
    status "Failed to mount niuser, creating new ext4 niuser file system"

    status "Running mkfs.ext4 on /dev/$niuser_part_device"
    mkfs.ext4 -q -F -L "niuser" "/dev/$niuser_part_device"

    sync

    # Try again
    if ! mount "/dev/$niuser_part_device" /mnt/niuser; then
        error "Failed to mount niuser after re-creating file system"
    fi
fi

niuser_lower_sha256sum=""
if [ -e "/mnt/niuser/overlay/lower.sha256sum" ]; then
    niuser_lower_sha256sum=$(head -c 65 "/mnt/niuser/overlay/lower.sha256sum")
else
    status "No /mnt/niuser/overlay/lower.sha256sum file"
fi

niboot_baserootfs_sha256sum=$(sha256sum "/mnt/niboot/baserootfs.squashfs" | cut -d" " -f1)

if [ "$niboot_baserootfs_sha256sum" != "$niuser_lower_sha256sum" ]; then
    status "Resetting upper file system due to different baserootfs image"

    rm -Rf /mnt/niuser/overlay
    mkdir  /mnt/niuser/overlay

    echo "$niboot_baserootfs_sha256sum" > "/mnt/niuser/overlay/lower.sha256sum"
fi

status "Create mount point for overlay"
mkdir -p /mnt/niuser/overlay/lower
mkdir -p /mnt/niuser/overlay/upper
mkdir -p /mnt/niuser/overlay/work
mkdir -p /mnt/niuser/overlay/image

status "Mount lower filesystem"
mount -o ro -t squashfs /mnt/niboot/baserootfs.squashfs /mnt/niuser/overlay/lower

status "Create overlay"
mount -t overlay -o lowerdir=/mnt/niuser/overlay/lower,upperdir=/mnt/niuser/overlay/upper,workdir=/mnt/niuser/overlay/work overlay /mnt/niuser/overlay/image

status "Move boot"
mount --move /mnt/niboot /mnt/niuser/overlay/image/boot

sync

status "Restore printk_devkmsg=$ORIG_KMSG_CONFIG"
echo "$ORIG_KMSG_CONFIG" > "/proc/sys/kernel/printk_devkmsg"

status "Running switch_root to /mnt/niuser/overlay/image/"
exec switch_root /mnt/niuser/overlay/image/ /sbin/init

