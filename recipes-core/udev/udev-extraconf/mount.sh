#!/bin/sh
#
# Called from udev
#
# Attempt to mount any added block devices and umount any removed devices


MOUNT="/bin/mount"
PMOUNT="/usr/bin/pmount"
UMOUNT="/bin/umount"
sgdisk_sed="s/^\/dev\/((sd[a-z]+)|(mmcblk[0-9]+)p)([0-9]+)$/sgdisk -i \4 \/dev\/\2\3/"
efi_guid="C12A7328-F81F-11D2-BA4B-00A0C93EC93B"
export PATH="$PATH:/usr/sbin"

[ "$DISABLE_AUTOMOUNT_BLACKLIST" ] || \
	for line in `grep -v ^# /etc/udev/mount.blacklist`
do
	if [ ` expr match "$DEVNAME" "$line" ` -gt 0 ];
	then
		logger "udev/mount.sh" "[$DEVNAME] is blacklisted, ignoring"
		exit 0
	fi
done

symlink_cleanup()
{
        local symlink_target

        # clear dangling compatibility symlinks
        for symlink in u v w x y z U V W X Y Z
        do
                if [ -h "/$symlink" ]; then
                        symlink_target=`readlink /$symlink`
                        if [ ! -e "$symlink_target" ]; then
                                rm "/$symlink"
                        else
                                grep -q -F "$symlink_target" /proc/mounts \
                                        || rm "/$symlink"
                        fi
                fi
        done
}

automount() {
	name="`basename "$DEVNAME"`"

	! test -d "/media/$name" && mkdir -p "/media/$name"
	# Silent util-linux's version of mounting auto
	if [ "x`readlink $MOUNT`" = "x/bin/mount.util-linux" ] ;
	then
		MOUNT="$MOUNT -o silent"
	fi

	case "$ID_FS_TYPE" in
		# If mounting vfat, set partition to be world-writable, and
		# disable permissions warnings
		vfat) MOUNT="$MOUNT -o fmask=0000,dmask=0000,quiet" ;;
	esac

	if ! $MOUNT -t auto $DEVNAME "/media/$name"
	then
		#logger "mount.sh/automount" "$MOUNT -t auto $DEVNAME \"/media/$name\" failed!"
		rm_dir "/media/$name"
	else
		logger "mount.sh/automount" "Auto-mount of [/media/$name] successful"
		touch "/tmp/.automount-$name"

		# create compatibility symlink
		for symlink in U V W X Y Z
		do
			symlink_lowercase=$(echo $symlink | tr '[:upper:]' '[:lower:]')
			ln -s "/media/$name" "/$symlink" && ln -s "/media/$name" "/$symlink_lowercase"
			if [ $? -eq 0 ]; then
				break
			fi
		done
	fi
}
	
rm_dir() {
	# We do not want to rm -r populated directories
	if test "`find "$1" | wc -l | tr -d " "`" -lt 2 -a -d "$1"
	then
		! test -z "$1" && rm -r "$1" && symlink_cleanup
	else
		logger "mount.sh/automount" "Not removing non-empty directory [$1]"
	fi
}

if [ "$ACTION" = "add" ] && [ -n "$DEVNAME" ] && [ -n "$ID_FS_TYPE" ]; then
	if [ -x "$PMOUNT" ]; then
		$PMOUNT $DEVNAME 2> /dev/null
	elif [ -x $MOUNT ]; then
    		$MOUNT $DEVNAME 2> /dev/null
	fi

	symlink_cleanup

	# check if sgdisk is available, if not, ignore the EFI check
	if command -v sgdisk > /dev/null; then
		# check if the device is the GRUB partition, if so, don't
		# automount -- this unreadable bit of shell scripting does
		# the following:
		# - use sed to find, for example, "sda" and "1" in $DEVNAME
		#   and generate an sgdisk command line to get info on that
		#   partition of that device
		# - grep the output of sgdisk to see if the partition is of
		#   type "EFI System" (by GUID) and if so, exit the script
		`echo $DEVNAME | sed -r -n "$sgdisk_sed"p` | grep -i "$efi_guid" -q && exit 0
	fi
	
	# If the device isn't mounted at this point, it isn't
	# configured in fstab (note the root filesystem can show up as
	# /dev/root in /proc/mounts, so check the device number too)
	if expr $MAJOR "*" 256 + $MINOR != `stat -c %d /`; then
		grep -q "^$DEVNAME " /proc/mounts || automount
	fi
fi



if [ "$ACTION" = "remove" ] && [ -x "$UMOUNT" ] && [ -n "$DEVNAME" ]; then
	for mnt in `cat /proc/mounts | grep "$DEVNAME" | cut -f 2 -d " " `
	do
		$UMOUNT $mnt
	done
	
	# Remove empty directories from auto-mounter
	name="`basename "$DEVNAME"`"
	test -e "/tmp/.automount-$name" && rm_dir "/media/$name"
fi


if [ "$ACTION" = "change" ] && [ -x "$UMOUNT" ] && [ -n "$DEVNAME" ]; then
	for mnt in `cat /proc/mounts | grep "$DEVNAME" | cut -f 2 -d " " `
	do
		$UMOUNT $mnt
	done

	# Remove empty directories from auto-mounter
	name="`basename "$DEVNAME"`"
	for mnt in `ls /tmp/.automount* | grep "$name" | cut -f 2 -d "-"`
	do
		rm_dir "/media/$mnt"
	done
fi
