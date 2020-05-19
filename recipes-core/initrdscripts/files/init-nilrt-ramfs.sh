#!/bin/bash

# Any failure at any line of this script will cause the script to
#  exit with error, this will then leads to a kernel panic
#  because it's the init script
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

function read_config_token() {
    local filename="$1"
    local token="$2"
    local value=""

    if [ -e "$filename" ]; then
        value=$(grep "^$token=" "$filename" | head -1 | cut -d"=" -f2) || value=""
    fi

    echo "$value"
}

function drop_config_token() {
    local filename="$1"
    local token="$2"

    if [ ! -e "$filename" ]; then
        return
    fi

    rm -f "$filename.new"
    cp -p "$filename" "$filename.new"

    sed -i "/^""$token""=/d" "$filename.new"

    mv "$filename.new" "$filename"
}

function read_file() {
    local filename="$1"
    local maxsize="$2"
    local value=""

    if [ -e "$filename" ]; then
        value=$(head -c "$maxsize" "$filename") || value=""
    fi

    echo "$value"
}

function read_sha256sum_file() {
    read_file "$1" 65
}

# Parse kernel cmd line to find nibootX partition's fs label
current_niboot_fs_label=$(cat /proc/cmdline | tr " " "\n" | grep "^rauc.slot=" | head -1 | cut -d= -f2)
current_niboot_fs_uuid=$(cat /proc/cmdline | tr " " "\n" | grep "^rauc.slot.uuid=" | head -1 | cut -d= -f2)

assert_valid_fs_id "rauc.slot" "$current_niboot_fs_label"
assert_valid_fs_id "rauc.slot.uuid" "$current_niboot_fs_uuid"

# Root device which is detected asynchronously may not show up
#  early, so continously polling for it until it is available
#  or 10s timeout.
status "Waiting for root device"
slumber=10
time_started=$SECONDS
while true; do
    # Find boot dev nodes using a combination of fs label and UUID so that
    #  multiple NILRT installations may co-exist on the same system
    current_niboot_part_device=$(lsblk -l -n -o NAME,LABEL,UUID | tr -s " " | egrep " $current_niboot_fs_label $current_niboot_fs_uuid\$" | head -1 | cut -d" " -f1) || true

    if [ -n "$current_niboot_part_device" ]; then
        # Found root device
        status "Root device detected."
        break
    fi

    time_elapsed=$((SECONDS - time_started))
    if [ $time_elapsed -ge $slumber ]; then
        error "Root device not found. System is unbootable."
    else
        # Sleep for 10ms each trial
        sleep 0.01
    fi
done

current_boot_disk_device=$(lsblk -l -n -o PKNAME "/dev/$current_niboot_part_device")

assert_valid_fs_id "current_boot_disk_device" "$current_boot_disk_device"

# Find niuser partition dev on same disk that we booted
niboota_part_device=$(lsblk -l -n -o NAME,PKNAME,PARTLABEL | tr -s " " | egrep " $current_boot_disk_device niboota\$" | head -1 | cut -d" " -f1)
nibootb_part_device=$(lsblk -l -n -o NAME,PKNAME,PARTLABEL | tr -s " " | egrep " $current_boot_disk_device nibootb\$" | head -1 | cut -d" " -f1)
niuser_part_device=$(lsblk -l -n -o NAME,PKNAME,PARTLABEL | tr -s " " | egrep " $current_boot_disk_device niuser\$" | head -1 | cut -d" " -f1)

status "Booting current_niboot_fs_label=$current_niboot_fs_label, current_niboot_fs_uuid=$current_niboot_fs_uuid, current_boot_disk_device=$current_boot_disk_device, current_niboot_part_device=$current_niboot_part_device, niuser_part_device=$niuser_part_device"

# Populate /dev/niboot with references
mkdir /dev/niboot

ln -sf "/dev/$niboota_part_device" /dev/niboot/niboota
ln -sf "/dev/$nibootb_part_device" /dev/niboot/nibootb
ln -sf "/dev/$niuser_part_device" /dev/niboot/niuser

if   [ "$current_niboot_part_device" == "$niboota_part_device" ]; then
    ln -sf niboota /dev/niboot/niboot.current
    ln -sf nibootb /dev/niboot/niboot.other
elif [ "$current_niboot_part_device" == "$nibootb_part_device" ]; then
    ln -sf niboota /dev/niboot/niboot.other
    ln -sf nibootb /dev/niboot/niboot.current
else
    warn "Unrecognized current_niboot_part_device=$current_niboot_part_device"
    ln -sf "/dev/$current_niboot_part_device" /dev/niboot/niboot.current
fi

# Mount niboot.current
readonly B_MNT="/mnt/niboot.current"
mkdir "$B_MNT"
mount -o ro,sync,relatime "/dev/niboot/niboot.current" "$B_MNT"

# Mount niuser
readonly U_MNT="/mnt/niuser"
mkdir "$U_MNT"

readonly U_OVERLAY_CFG="$U_MNT/overlay/upper/etc/niboot/init-action.cfg"

function mount_niuser_helper() {
    mount -o rw,sync,relatime "/dev/niboot/niuser" "$U_MNT"
}

do_format_niuser=false

if mount_niuser_helper; then
    if [ "$(read_config_token "$U_OVERLAY_CFG" reformat_niuser)" == "true" ]; then
        status "init-action.cfg directed reformat of niuser"
        umount "$U_MNT"
        do_format_niuser=true
    fi
else
    status "Failed to mount niuser, reformatting"
    do_format_niuser=true
fi

if $do_format_niuser; then
    mkfs.ext4 -q -F -L "niuser" "/dev/niboot/niuser"

    # Try to mount again
    if ! mount_niuser_helper; then
        error "Failed to mount niuser after re-creating file system"
    fi
fi

current_baserootfs_sha256sum=$(sha256sum "$B_MNT/baserootfs.squashfs" | cut -d" " -f1)

do_reset_overlay=false

# First check if we are explicitly directed to reset the overlay
if [ "$(read_config_token "$U_OVERLAY_CFG" reset_overlay)" == "true" ]; then
    status "init-action.cfg directed reset of upper file system"
    do_reset_overlay=true
else
    # otherwise verify the baserootfs checksum matches
    current_overlay_sha256sum=$(read_sha256sum_file "$U_MNT/overlay/lower.sha256sum")
    if [ "$current_overlay_sha256sum" == "$current_baserootfs_sha256sum" ]; then
        status "Current overlay matches baserootfs"
    else
        # Bad current overlay checksum, lets see if we can fall back to old overlay
        old_overlay_sha256sum=$(read_sha256sum_file "$U_MNT/overlay.old/lower.sha256sum")
        if [ "$old_overlay_sha256sum" == "$current_baserootfs_sha256sum" ]; then
            status "Old overlay matches baserootfs, falling back"

            # Remove current overlay if it exists
            # This is fail safe because the condition that got us here still hold:
            #  - do_reset_overlay will stay false, that's the default
            #  - current_overlay_sha256sum will become ""
            #  - old_overlay_sha256sum will stay the same
            rm -Rf "$U_MNT/overlay"

            # Promote old overlay to current
            mv "$U_MNT/overlay.old" "$U_MNT/overlay"
        else
            status "Resetting upper file system due to different baserootfs image"
            do_reset_overlay=true
        fi
    fi
fi

if $do_reset_overlay; then
    # Demote current overlay to old if it exist, removing any old
    #  overlay which may be present
    if [ -e "$U_MNT/overlay" ]; then
        rm -Rf "$U_MNT/overlay.old"
        mv "$U_MNT/overlay" "$U_MNT/overlay.old"
    fi

    # Create new overlay with current baserootfs hash
    # This is done as an atomic operation to facilitate recovery (above)
    #  after catastrophic failure like power loss
    rm -Rf "$U_MNT/overlay.new"
    mkdir "$U_MNT/overlay.new"
    echo "$current_baserootfs_sha256sum" > "$U_MNT/overlay.new/lower.sha256sum"
    mv "$U_MNT/overlay.new" "$U_MNT/overlay"
fi

boot_safemode=false

if [ "$(read_config_token "$U_OVERLAY_CFG" boot_safemode)" == "true" ]; then
    status "Booting safemode, directed by user config"
    drop_config_token "$U_OVERLAY_CFG" boot_safemode
    boot_safemode=true
fi

for file in /sys/bus/acpi/drivers/nirtfeatures/*/safe_mode; do
    status=$(read_file "$file" 10)
    if [ "$status" == "1" ]; then
        status "Booting safemode, directed by firmware ($file = 1)"
        boot_safemode=true
    fi
done

init_options=""

if $boot_safemode; then
    init_options="4"
fi

for file in /sys/bus/acpi/drivers/nirtfeatures/*/ip_reset; do
    status=$(read_file "$file" 10)
    if [ "$status" == "1" ]; then
        status "Reset network configuration, directed by firmware ($file = 1)"
        if [  -e "$U_MNT/overlay/upper/var/lib/connman/"  ]; then
            find "$U_MNT/overlay/upper/var/lib/connman/" -mindepth 1 -maxdepth 1 -exec rm -Rf {} \;
        fi
    fi
done

status "Create mount point for overlay"
mkdir -p "$U_MNT/overlay/lower"
mkdir -p "$U_MNT/overlay/upper"
mkdir -p "$U_MNT/overlay/work"
mkdir -p "$U_MNT/overlay/image"

status "Mount lower filesystem"
mount -o ro -t squashfs "$B_MNT/baserootfs.squashfs" "$U_MNT/overlay/lower"

status "Create overlay image"
mount -t overlay -o lowerdir="$U_MNT/overlay/lower,upperdir=$U_MNT/overlay/upper,workdir=$U_MNT/overlay/work" overlay "$U_MNT/overlay/image"

status "Move boot"
mount --move "$B_MNT" "$U_MNT/overlay/image/boot"

# Remove sync option from niuser mount in preparation for toggle
sync
mount -o remount,async "$U_MNT"

status "Restore printk_devkmsg=$ORIG_KMSG_CONFIG"
echo "$ORIG_KMSG_CONFIG" > "/proc/sys/kernel/printk_devkmsg"

status "Running switch_root to $U_MNT/overlay/image/"
exec switch_root "$U_MNT/overlay/image/" /sbin/init $init_options

