#!/bin/sh

mountdir="/media"

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
	[ ! -b "${MDEV}" ] && exit 1

	# check if the device is already mounted; if so, don't automount it
	for f in `cut -f1 -d\  < /etc/mtab | grep /dev/`; do
		[ -h "$f" ] && f="`readlink -f $f`"
		if [ "x$f" = "x/dev/$1" ] ; then
			exit 1
		fi
	done

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
