# Shell functions related to nisystemformat 'format' operations.


# ==============================================================================
# CONSTANTS
# ==============================================================================

ARCH=${ARCH:=$(uname -m)}

ACCTINFO_TMP=/tmp/acctinfo

if [ "$ARCH" = "x86_64" ]; then
	CONFIGFS_DEV=/dev/disk/by-label/niconfig
	ROOTFS_DEV=/dev/disk/by-label/nirootfs
fi

export CONFIG_MOUNT_POINT="/etc/natinst/share"
export ROOTFS_MOUNT_POINT="/mnt/userfs"


# ==============================================================================
# FUNCTIONS
# ==============================================================================

# Format the config volume
function format_config()
{
	local fstype="$1"  # filesystem type
	local configfs_dev="${2:-$CONFIGFS_DEV}"  # device to format (optional, defaults to $CONFIGFS_DEV)
	# fstype is validated before calling format_config
	case "$1" in
	  ubifs)
		local partition_label="boot-config"
		local volume_number="3"
		local volume_label="config"
		format_ubi_volume "$partition_label" "$volume_number" "$volume_label"
		;;
	  ext4)
		local volume_label="niconfig"
		local options=""
		mkfs.ext4 -q -F -I 256 -L $volume_label $options ${2:-$CONFIGFS_DEV}
		;;
	esac
}

# Format the rootfs volume
function format_rootfs()
{
	local fstype="$1"  # filesystem type
	local rootfs_dev="${2:-$ROOTFS_DEV}"  # device to format (optional)
	# remove Zynq kernel
	rm -f /boot/linux_runmode.itb

	# remove any old kernel-dev files which were deposited in the /boot partition
	rm -f /boot/Module.symvers-*
	rm -f /boot/config-*
	rm -f /boot/System.map-*

	# remove x64 kernel
	if [ -e /boot/runmode  ]; then
		rm -R /boot/runmode
		mkdir /boot/runmode
	fi

	# fstype is validated before calling format_rootfs
	case "$fstype" in
	  ubifs)
		local partition_label="root"
		local volume_number="0"
		local volume_label="rootfs"
		format_ubi_volume "$partition_label" "$volume_number" "$volume_label"
		;;
	  ext4)
		local volume_label="nirootfs"
		local options=""
		mkfs.ext4 -q -F -L $volume_label $options "${rootfs_dev}"
		;;
	esac
}


# Top-level entry point for reformats. Stop storage-sensitive system services
# prior to the format attempt. Restart services on success if requested, or on
# failure if appropriate.
function format_rootfs_or_userfs()
{
	[[ $(supported_fstypes) =~ (^|,)$TYPE(,) ]] || die_with_usage INVALID_FSTYPE "Invalid fstype '$TYPE'"

	/etc/init.d/systemWebServer stop ||:
	/etc/init.d/sshd stop ||:
	/etc/init.d/niauth stop ||:

	# ABORTED_ATTEMPT=yes means something went wrong before we actually reformatted
	# anything: go ahead and restart all services. We set it at the outset,
	# so that any premature failure will set it by default, and unset it once it
	# is no longer applicable.
	ABORTED_ATTEMPT=yes
	local ret=0
	format_rootfs_or_userfs_nosvc || ret=$?
	if (( ! ret )) && [ "$RELAUNCH" = yes ] || [ "$ABORTED_ATTEMPT" = yes ]
	then
		/etc/init.d/niauth start ||:
		/etc/init.d/sshd start ||:
		/etc/init.d/systemWebServer start ||:
	fi
	return $ret
}
export -f format_rootfs_or_userfs


# Format operation, assuming all impacted mountpoints are unmounted. This is
# what actually does the formatting.
function format_rootfs_or_userfs_nomount()
{
	ABORTED_ATTEMPT=	# After this point, failures are unrecoverable

	configfs_dev="$CONFIGFS_DEV"
	rootfs_dev="$ROOTFS_DEV"

	if [ "$VOL" = config ]; then
		format_config "$TYPE" "$configfs_dev"
	else
		format_rootfs "$TYPE" "$rootfs_dev"
	fi
}


# Format operation, assuming all impacted services are shut down. Saves and
# restores system configuration, if necessary.
function format_rootfs_or_userfs_nosvc()
{
	netconfig_pre || return $?

	# Backup the shared .shadow file to the tmp dir.
	if [ -f "$CONFIG_MOUNT_POINT/.shadow" ]; then
		trap 'rm -f "$ACCTINFO_TMP/.shadow"' EXIT HUP INT TERM
		mkdir -p "$ACCTINFO_TMP" &&
			cp "$CONFIG_MOUNT_POINT/.shadow" "$ACCTINFO_TMP" ||
			return $?
	fi

	# Backup the restore files to the tmp dir. Do this even if we're only
	# reformatting the configfs, because under some configurations,
	# reformatting the configfs may also trigger reformatting the userfs.
	# (And it doesn't cost us anything to do the copy)
	if [ -e "$ROOTFS_MOUNT_POINT/.restore" ]; then
		mkdir -p /tmp/restore &&
			cp -R "$ROOTFS_MOUNT_POINT"/.restore/* /tmp/restore/ ||
			return $?
	fi

	# If we errored out, either the error was unrecoverable (in which case
	# the rest of this should be skipped), or recoverable (in which case no
	# reformat happened and there is no need to restore configurations)
	format_rootfs_or_userfs_nosvc_noconf || return $?

	local ret=0

	# Move the restore files back to the rootfs. Only do this if the
	# reformat actually happened, i.e. /.restore doesn't exist. In order to
	# make a best-effort attempt at recovery, try to move restore files back
	# to the rootfs (if it exists) even if mountconfig failed.
	if [ -e /tmp/restore -a ! -e "$ROOTFS_MOUNT_POINT/.restore" ] &&
		   mountpoint -q "$ROOTFS_MOUNT_POINT"
	then
		mkdir -p "$ROOTFS_MOUNT_POINT/.restore" &&
			mv /tmp/restore/* "$ROOTFS_MOUNT_POINT/.restore" &&
			rmdir /tmp/restore || (( ret )) || ret=$?
	fi

	# Move the .shadow file back to the configfs.
	if [ -f "$ACCTINFO_TMP/.shadow" -a ! -f "$CONFIG_MOUNT_POINT/.shadow" ] &&
		   mountpoint -q "$CONFIG_MOUNT_POINT"
	then
		mv -f "$ACCTINFO_TMP/.shadow" $CONFIG_MOUNT_POINT &&
			rmdir $ACCTINFO_TMP	|| (( ret )) || ret=$?
	fi

	netconfig_post || (( ret )) || ret=$?
	# targetinfo.ini needs to be restored or it will not be recreated until a reboot into safemode
	targetinfo_restore || (( ret )) || ret=$?
	return $ret
}


# Format operation, assuming all impacted services are shut down, and all
# impacted system configuration has been saved off.
function format_rootfs_or_userfs_nosvc_noconf() {
	local ret=0
	if \
		with_retry /etc/init.d/populateconfig stop &&
		with_retry /etc/init.d/mountconfig stop &&
		with_retry /etc/init.d/mountcompatibility stop &&
		with_retry /etc/init.d/mountuserfs stop
	then
		format_rootfs_or_userfs_nomount || (( ret )) || ret=$?
	else
		ret=$?
	fi
	# If we are erroring out unrecoverably, skip the rest of this function
	(( ret )) && ! [ "$ABORTED_ATTEMPT" = yes ] && return $ret

	# configfs needs to be mounted before rootfs otherwise only one
	# *etc/natinst/share will be mounted. (This may be a bug in the mount*
	# initscripts.)
	with_retry /etc/init.d/mountconfig start &&
		with_retry /etc/init.d/populateconfig start || (( ret )) || ret=$?
	with_retry /etc/init.d/mountuserfs start &&
		with_retry /etc/init.d/mountcompatibility start || (( ret )) || ret=$?
	return $ret
}


# Find the filesystem type of the config partition (or volume)
function print_config_fstype()
{
	grep " $CONFIG_MOUNT_POINT " /proc/mounts | awk '{print $3}'
}
export -f print_config_fstype


# Find the filesystem type of the root partition (or volume)
function print_root_fstype()
{
	if [ "$OSMODE" != runmode ]; then
		grep " $ROOTFS_MOUNT_POINT " /proc/mounts | awk '{print $3}'
	else
		# regarding skipping rootfs, see "What is rootfs?" at
		# https://www.kernel.org/doc/Documentation/filesystems/ramfs-rootfs-initramfs.txt
		grep " / " /proc/mounts | awk '{if ($3 != "rootfs") print $3}'
	fi
}
export -f print_root_fstype


# Print the supported filesystem types for this system.
# The output is a comma-separated list of filesystem types,
# with no trailing comma.
function supported_fstypes()
{
	hash /usr/sbin/ubiformat 2>/dev/null && [ "$ARCH" = "armv7l" ] && echo -n "ubifs," || true
	hash /sbin/mkfs.ext4 2>/dev/null && [ "$ARCH" = "x86_64" ] && echo -n "ext4," || true
}
export -f supported_fstypes

