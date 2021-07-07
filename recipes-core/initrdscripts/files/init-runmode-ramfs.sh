#!/bin/bash

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
echo on >"/proc/sys/kernel/printk_devkmsg"
status "Set printk_devkmsg=on (previous: $ORIG_KMSG_CONFIG)"

ARCH="`uname -m`"
status "Running init process on ARCH=$ARCH"

if [ "$ARCH" == "x86_64" ]; then
	# Root device which is detected asynchronously may not show up
	#  early, so continously polling for it until it is available
	#  or 10s timeout.
	status "Waiting for root device"
	slumber=10
	time_started=$SECONDS
	while true; do
		rootdevice_list="`lsblk -l -n -o NAME,PARTUUID | tr -s " " | grep " $rootuuid"`"

		if [ -n "$rootdevice_list" ]; then
			# Found root device
			status "Root device detected."
			break
		fi

		time_elapsed=$((SECONDS - time_started))
		if [ $time_elapsed -ge $slumber ]; then
			error "Root device not found. System is unbootable."
			# Let the system hang while still displaying the message,
			#  since we have no way to force into safemode.
			while true; do true; done
		else
			# Sleep for 10ms each trial
			sleep 0.01
		fi
	done
fi

status "Setting next boot to safemode in case booting runmode fails"
mkdir -p /mnt/boot
mount -L nibootfs -o rw,sync /mnt/boot
echo -n "" >/mnt/boot/bootflags.d/safemode
rm -f /mnt/boot/bootflags.d/runtime
umount /mnt/boot

# expected by nilrtdiskcrypt
status "Mount /var/volatile as tmpfs"
mkdir -p /var/volatile
mount -t tmpfs tmpfs /var/volatile

if [ "$ARCH" == "x86_64" ]; then
	status "Running depmod"
	depmod -a

	bootdevice=""

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

		status "Reseal runmode key"
		nilrtdiskcrypt_reseal -u 1

		# NILRT on rootfs will mount niconfig by label. Verify niconfig
		#  also opens or do not open either.
		configdevice="/dev/$(lsblk -l -n -o PARTLABEL,NAME | grep '^niconfig '| tr -s ' ' | cut -d' ' -f2)"
		status "Found configdevice=$configdevice"

		status "Check for encrypted disks at rootdevice=$rootdevice and configdevice=$configdevice"
		if nilrtdiskcrypt_canopen -d "$rootdevice" -d "$configdevice"; then
			status "Open partitions rootdevice=$rootdevice and configdevice=$configdevice"
			cryptdevs=( $(nilrtdiskcrypt_open -k 1 -d "$rootdevice" -d "$configdevice") )

			do_failsafe=false

			[ -n "${cryptdevs[0]}" ] || do_failsafe=true
			[ -n "${cryptdevs[1]}" ] || do_failsafe=true

			[ "`lsblk -l -n -o LABEL | egrep "^nirootfs$" | wc -l`" == 1 ] || do_failsafe=true
			[ "`lsblk -l -n -o LABEL | egrep "^niconfig$" | wc -l`" == 1 ] || do_failsafe=true

			lsblk -l -n -o LABEL,TYPE,NAME | egrep -q "^nirootfs +crypt +""`basename "${cryptdevs[0]}"`""\$" || do_failsafe=true
			lsblk -l -n -o LABEL,TYPE,NAME | egrep -q "^niconfig +crypt +""`basename "${cryptdevs[1]}"`""\$" || do_failsafe=true

			# all or nothing failsafe
			if $do_failsafe; then
				nilrtdiskcrypt_close -d "$rootdevice"
				nilrtdiskcrypt_close -d "$configdevice"
				nilrtdiskcrypt_disableunseal

				error "Failed to open nirootfs and niconfig"
			else
				bootdevice="${cryptdevs[0]}"
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
		umount /var/volatile
		echo "$ORIG_KMSG_CONFIG" >"/proc/sys/kernel/printk_devkmsg"
		exec switch_root /mnt/root /sbin/init
	else
		error "No /mnt/root/sbin/init, cannot boot bootdevice=$bootdevice"
		error "System is unbootable. Force safemode and reformat."
	fi
else
	error "ARCH=$ARCH is not supported by this initramfs"
fi

# cleanup
umount -a -r
sync

sleep 10
reboot -f
