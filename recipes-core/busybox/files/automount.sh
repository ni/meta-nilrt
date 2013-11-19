#!/bin/sh

mountdir="/media"
sgdisk_sed="s/^(sd[a-z])([0-9]+)$/sgdisk -i \2 \/dev\/\1/"
efi_guid="C12A7328-F81F-11D2-BA4B-00A0C93EC93B"

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

add_action()
{
	# check if the device exists; otherwise exit
	[ ! -b "$1" ] && exit 1

	# check if the device is already mounted; if so, don't automount it
	for f in `cut -f1 -d\  < /etc/mtab | grep /dev/`; do
		[ -h "$f" ] && f="`readlink -f $f`"
		if [ "x$f" = "x/dev/$1" ] ; then
			exit 1
		fi
	done

	# check if sgdisk is available, if not, ignore the EFI check
	if command -v sgdisk > /dev/null; then
		# check if the device is the GRUB partition, if so, don't
		# automount -- this unreadable bit of shell scripting does
		# the following:
		# - use sed to find, for example, "sda" and "1" in $MDEV
		#   and generate an sgdisk command line to get into on that
		#   partition of that device
		# - grep the output of sgdisk to see if the partition is of
		#   type "EFI System" (by GUID) and if so, exit the script
		`echo $1 | sed -r -n "$sgdisk_sed"p` | grep -i "$efi_guid" > /dev/null && exit 1
	fi

	symlink_cleanup

	mkdir -p "${mountdir}/$1" || exit 1

	if ! mount -t auto "/dev/$1" "${mountdir}/$1"; then
		rmdir "${mountdir}/$1"
		exit 1
	fi

	# create compatibility symlinks
	for symlink in U V W X Y Z
	do
		symlink_lowercase=$(echo $symlink | tr '[:upper:]' '[:lower:]')
		if [ ! -e "/$symlink" -a ! -e "/$symlink_lowercase" ]; then
			ln -s "${mountdir}/$1" "/$symlink"
			ln -s "${mountdir}/$1" "/$symlink_lowercase"
			break
		fi
	done
}

remove_action()
{
	if grep -qs "^/dev/$1 " /proc/mounts ; then
		umount "${mountdir}/$1";
	fi

	[ -d "${mountdir}/$1" ] && rmdir "${mountdir}/$1"

	symlink_cleanup
}

case "${ACTION}" in
	add|"")
		add_action "${MDEV}"
		;;
	remove)
		remove_action "${MDEV}"
		;;
esac
