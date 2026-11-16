#!/bin/bash

source $(dirname "$0")/ptest-format.sh

ptest_change_test $(basename "$0" ".sh") "" "Check SystemSettings worldwritable.enabled controls block device automount"

NIRTCFG=/usr/local/natinst/bin/nirtcfg
IMG=$(mktemp /tmp/automount-test.XXXXXX)
DMNAME="automount-test-$$"
LOOPDEV=""
DMDEV=""
ORIG_VALUE=$($NIRTCFG --get section=SystemSettings,token=worldwritable.enabled,value=__unset__)

# $1 = worldwritable.enabled value to set, or "unset" to remove the key
set_toggle()
{
	if [ "$1" = unset ]; then
		$NIRTCFG --clear section=SystemSettings,token=worldwritable.enabled
	else
		$NIRTCFG --set section=SystemSettings,token=worldwritable.enabled,value=$1
	fi
}

# /proc/mounts may name the device either /dev/dm-N or /dev/mapper/<name>
dm_mountpoints()
{
	awk -v a="$DMDEV" -v b="/dev/mapper/$DMNAME" '$1 == a || $1 == b { print $2 }' /proc/mounts
}

unmount_dm()
{
	local mnt
	for mnt in $(dm_mountpoints); do
		umount "$mnt"
	done
	rm -f "/tmp/.automount-${DMDEV##*/}"
	rmdir "/media/${DMDEV##*/}" 2> /dev/null
	for link in /[UVWXYZuvwxyz]; do
		[ "$(readlink "$link")" = "/media/${DMDEV##*/}" ] && rm -f "$link"
	done
}

# Loop devices are on the automount blacklist, so the image is exposed through a device-mapper device.
setup_dm()
{
	local sectors
	LOOPDEV=$(losetup --find --show "$IMG") || return 1
	sectors=$(( $(stat -c %s "$IMG") / 512 ))
	dmsetup create "$DMNAME" --table "0 $sectors linear $LOOPDEV 0" || return 1
	DMDEV=/dev/dm-$(dmsetup info -c --noheadings -o minor "$DMNAME")
}

teardown_dm()
{
	[ -n "$DMDEV" ] && unmount_dm
	dmsetup info "$DMNAME" > /dev/null 2>&1 && dmsetup remove "$DMNAME"
	[ -n "$LOOPDEV" ] && losetup -d "$LOOPDEV"
	DMDEV=""
	LOOPDEV=""
}

cleanup()
{
	teardown_dm
	rm -f "$IMG"
	if [ "$ORIG_VALUE" = __unset__ ]; then
		set_toggle unset
	else
		set_toggle "$ORIG_VALUE"
	fi
}
trap cleanup EXIT

# $1 = worldwritable.enabled value to set (or "unset"), $2 = expected state (mounted|unmounted)
run_case()
{
	local mounted=unmounted
	set_toggle "$1" || return 1
	if ! udevadm trigger --action=add "/sys/class/block/${DMDEV##*/}" || ! udevadm settle --timeout=60; then
		echo "udev add event for $DMDEV was not processed"
		unmount_dm
		return 1
	fi
	[ -n "$(dm_mountpoints)" ] && mounted=mounted
	unmount_dm
	[ "$mounted" = "$2" ]
}

if ! truncate -s 16M "$IMG" || ! mkfs.ext4 -q -F "$IMG" > /dev/null 2>&1; then
	echo "could not create test filesystem image $IMG"
	ptest_fail
	ptest_report
	exit 1
fi

# Create the device with the toggle off so the creation event does not mount it.
if ! set_toggle false || ! setup_dm; then
	echo "could not create device-mapper test device for $IMG"
	ptest_fail
	ptest_report
	exit 1
fi

ptest_change_subtest 1 "worldwritable.enabled=false: block device is not mounted"
if run_case false unmounted; then
	ptest_pass
else
	ptest_fail
fi
ptest_report

ptest_change_subtest 2 "worldwritable.enabled=true: block device is mounted"
if run_case true mounted; then
	ptest_pass
else
	ptest_fail
fi
ptest_report

ptest_change_subtest 3 "worldwritable.enabled unset (default): block device is not mounted"
if run_case unset unmounted; then
	ptest_pass
else
	ptest_fail
fi
ptest_report
