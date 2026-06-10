# Shell functions related to nisystemformat 'format' operations.
# The main entrypoint for this library is ``format_rootfs_or_userfs``.


# ==============================================================================
# CONSTANTS
# ==============================================================================

ARCH=${ARCH:=$(uname -m)}

ACCTINFO_TMP=/tmp/acctinfo

# The *partition* labels for each logical volume.
# If LUKS encryption is used, the LUKS partition labels will have "-luks" appended.
USERFS_PARTLABEL=nirootfs
NICONFIG_PARTLABEL=niconfig
# The *Filesystem* labels for each logical volume.
# These will also be the dm mapping names if encryption is used.
USERFS_LABEL=nirootfs
NICONFIG_LABEL=niconfig

# check for artemis compatibility from devicetree
# set mountfs to ext4 if artemis model is detected
# this allows fstype to return ext4 instead of ubifs
if grep -qs artemis /sys/firmware/devicetree/base/compatible ; then
	mountfs=ext4
fi

if [ "$ARCH" = "x86_64" ] || [ "${mountfs:-}" = "ext4" ]; then
	CONFIGFS_DEV=/dev/disk/by-partlabel/$NICONFIG_PARTLABEL
	ROOTFS_DEV=/dev/disk/by-partlabel/$USERFS_PARTLABEL
fi

export CONFIG_MOUNT_POINT="/etc/natinst/share"
export ROOTFS_MOUNT_POINT="/mnt/userfs"


# ==============================================================================
# IMPORTS
# ==============================================================================

source ${BASH_SOURCE%/*}/exit_codes.sh || exit 8
source ${BASH_SOURCE%/*}/encryption.sh || exit ${EXITCODES[IMPORT_ERROR]}
source ${BASH_SOURCE%/*}/netconfig.sh || exit ${EXITCODES[IMPORT_ERROR]}


# ==============================================================================
# FUNCTIONS
# ==============================================================================

# Convert a device partition to a LUKS encrypted volume, using TPM-based key
# management.
function _convert_to_luks() {
	local dev="$1"  # the block device to convert (e.g. /dev/sda1)
	local fslabel="$2"

	# If the USERFS partition is not a LUKS volume, make sure to remove its FSLabel
	if ! cryptsetup isLuks "$dev" 2>/dev/null; then
		log INFO "Removing existing filesystem label from $dev (if any)"
		e2label "$dev" "" 2>/dev/null
	fi

	# Generate a random master key, which we will discard later.
	master_keyfile=$(mktemp -p /dev/shm ni-systemformat.masterkey.XXXXXX)
	chmod 0600 "$master_keyfile"
	dd if=/dev/urandom of="$master_keyfile" bs=512 count=1 2>/dev/null || \
		die UNKNOWN_ERROR "Failed to generate random master key"

	# TODO: restrict cipher characteristics
	cryptsetup luksFormat \
		--encrypt \
		--label "$fslabel-luks" \
		--type luks2 \
		--key-slot 0 \
		--key-size 256 \
		--cipher aes-xts-plain64 \
		--batch-mode \
		--key-file "$master_keyfile" \
		"$dev"
	ni-reseal-luks --yes "$master_keyfile"
	clevis luks unlock \
		-d "$dev" \
		-n "$fslabel"

	# Best-effort to remove the master keyfile, but it is on a tmpfs anyway.
	rm -f "$master_keyfile"
	
	ROOTFS_DEV="/dev/mapper/$fslabel"
}


# Format the config volume
function format_config()
{
	local fstype="$1"  # filesystem type
	local configfs_dev="${2:-$CONFIGFS_DEV}"  # device to format (optional, defaults to $CONFIGFS_DEV)

		# Optionally convert the userfs to a LUKS volume and mount it.
	if [ "$OPT_ENCRYPT" = yes ]; then
		log INFO "Encrypting $configfs_dev with LUKS..."
		_convert_to_luks "$configfs_dev" "$NICONFIG_PARTLABEL" \
			|| die UNKNOWN_ERROR "Failed to convert configfs partition to LUKS."
		log INFO "DONE"
		configfs_dev="$CONFIGFS_DEV"  # update configfs_dev to point to the new mapper device
	else
		if cryptsetup isLuks "$configfs_dev" 2>/dev/null; then
			log INFO "Converting $configfs_dev back to non-LUKS."
			cryptsetup erase -q "${configfs_dev}"
		fi
	fi

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

	# Optionally convert the userfs to a LUKS volume and mount it.
	if [ "$OPT_ENCRYPT" = yes ]; then
		log INFO "Encrypting $rootfs_dev with LUKS..."
		_convert_to_luks "$rootfs_dev" "$USERFS_PARTLABEL" \
			|| die UNKNOWN_ERROR "Failed to convert rootfs partition to LUKS."
		log INFO "DONE"
		rootfs_dev="$ROOTFS_DEV"  # update rootfs_dev to point to the new mapper device
	else
		if cryptsetup isLuks "$rootfs_dev" 2>/dev/null; then
			log INFO "Converting $rootfs_dev back to non-LUKS."
			cryptsetup erase -q "${rootfs_dev}"
		fi
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
		mkfs.ext4 -q -F -L "$volume_label" $options "$rootfs_dev"
		;;
	esac
}


# Top-level entry point for reformats. Stop storage-sensitive system services
# prior to the format attempt. Restart services on success if requested, or on
# failure if appropriate.
# Returns: 0 on success, nonzero on failure.
function format_rootfs_or_userfs() {
	# ABORTED_ATTEMPT=yes means something went wrong before we actually reformatted
	# anything: go ahead and restart all services. We set it at the outset,
	# so that any premature failure will set it by default, and unset it once it
	# is no longer applicable.
	ABORTED_ATTEMPT=yes
	local ret=0  # function return code.
	
	# REQUEST VALIDATION
	[[ $(supported_fstypes) =~ (^|,)$TYPE(,) ]] || \
		die_with_usage INVALID_FSTYPE "Invalid fstype '$TYPE'"
	if [ "$OPT_ENCRYPT" = yes ]; then
		check_tpm || die BAD_ENVIRONMENT \
			"Encryption was requested but no TPM device was found."
		type cryptsetup >/dev/null || die BAD_ENVIRONMENT \
			"Encryption was requested but cryptsetup is not installed."
	fi
	# /REQUEST VALIDATION

	# Stop services that may be impacted by the format operation.
	trap "_necessary_services_start" EXIT
	_necessary_services_stop
	# Save off the current network configuration.
	netconfig_pre
	trap "netconfig_post; _necessary_services_start" EXIT

	# Do the actual format.
	format_rootfs_or_userfs_nosvc || ret=$?

	# Restore the network configuration.
	netconfig_post || (( ret )) || ret=$?
	# Restart services
	_necessary_services_start
	trap - EXIT

	# targetinfo.ini needs to be restored or it will not be recreated until
	# a reboot into safemode
	targetinfo_restore

	return $ret
}


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
	return $ret
}


# Format operation, assuming all impacted services are shut down, and all
# impacted system configuration has been saved off.
function format_rootfs_or_userfs_nosvc_noconf() {
	local ret=0
	if _unmount_volumes; then
		format_rootfs_or_userfs_nomount || (( ret )) || ret=$?
	else
		ret=$?
	fi
	# If we are erroring out unrecoverably, skip the rest of this function
	(( ret )) && ! [ "$ABORTED_ATTEMPT" = yes ] && return $ret

	# configfs needs to be mounted before rootfs otherwise only one
	# *etc/natinst/share will be mounted. (This may be a bug in the mount*
	# initscripts.)
	_mount_volumes || (( ret )) || ret=$?
	return $ret
}


# Find the filesystem type of the config partition (or volume)
function print_config_fstype()
{
	grep " $CONFIG_MOUNT_POINT " /proc/mounts | awk '{print $3}'
}


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

function _necessary_services_stop() {
	/etc/init.d/systemWebServer stop
	# SSHd uses start-stop-daemon without the --oknodo arg, so returns nonzero
	# if the daemon is not running.
	# That's OK. So use the initscript's 'status' command to double-check.
	if ! /etc/init.d/sshd stop; then
		/etc/init.d/sshd status && [ $? -eq 3 ]
	fi
	/etc/init.d/niauth stop
}


function _necessary_services_start() {
	with_retry /etc/init.d/niauth start
	with_retry /etc/init.d/sshd start
	with_retry /etc/init.d/systemWebServer start
}

# Print the supported filesystem types for this system.
# The output is a comma-separated list of filesystem types,
# with no trailing comma.
function supported_fstypes()
{
	if [ "$ARCH" = "armv7l" ]; then
		if [ "${mountfs:-}" = "ext4" ]; then # Adding $mountfs condition to properly report fstype for Artemis
			hash /sbin/mkfs.ext4 2>/dev/null && echo -n "ext4," || true
		else
			hash /usr/sbin/ubiformat 2>/dev/null && echo -n "ubifs," || true
		fi
	fi
	hash /sbin/mkfs.ext4 2>/dev/null && [ "$ARCH" = "x86_64" ] && echo -n "ext4," || true
}


# Restore the targetinfo.ini file on the configfs.
function targetinfo_restore()
{
	SUPPORTED_FS=$(supported_fstypes)
	CURRENT_FS=$(print_root_fstype)
	cat >"$TARGETINFO_PATH" <<-EOF
	[FileSystem]
	Current=$CURRENT_FS
	Supported=$SUPPORTED_FS
	EOF
}

function _mount_volumes() {
	local ret=0
	# Mount volumes in the rcS.d order.
	with_retry /etc/init.d/mountconfig start || ret=$?
	with_retry /etc/init.d/populateconfig start || ret=$?
	with_retry /etc/init.d/mountuserfs start || ret=$?
	with_retry /etc/init.d/mountcompatibility start || ret=$?
	return $ret
}


# Unmount volumes in reverse rcS.d order.
# Handle open crypto mappings as well.
# Returns: 0 on success, nonzero on failure.
function _unmount_volumes() {
	# Unount volumes in reverse rcS.d order.
	with_retry /etc/init.d/mountcompatibility stop || return $?
	with_retry /etc/init.d/mountuserfs stop || return $?
	with_retry /etc/init.d/populateconfig stop || return $?
	with_retry /etc/init.d/mountconfig stop || return $?

	# Close open crypto mappings
	if type cryptsetup >/dev/null 2>&1; then
		for fslabel in $USERFS_LABEL $NICONFIG_LABEL; do
			dmsetup ls --target crypt | grep -q "^$fslabel\s" || continue
			log INFO "Closing crypto mapping for $fslabel"
			cryptsetup close -q "$fslabel" || return $?
		done
	fi

	return 0
}


# ==============================================================================
# EXPORTS
# ==============================================================================

export -f format_rootfs_or_userfs
export -f print_config_fstype
export -f print_root_fstype
export -f supported_fstypes
export -f targetinfo_restore
