#!/bin/bash

# Change root pwd to / where ni_provisioning is located
cd /

# Add reboot/poweroff functions since we don't have sysvinit in restore
reboot() {
	echo 'Unmounting all file systems'
	# In recovery / /proc /sys /dev are all bound to RAM, no unmount is necessary
	# From a bash interactive login, "umount -a -r" will continue to run following commands
	# In /ni_provisioning context called by /init, "umount -a -r" will stop /ni_provisioning
	# from running next commands
	umount -a -r -t norootfs,noproc,nosysfs,nodevtmpfs
	sync
	echo 'Rebooting'
	command reboot -f
	sleep 1
	echo b > /proc/sysrq-trigger
	sleep 1
}

poweroff() {
	echo 'Unmounting all file systems'
	umount -a -r -t norootfs,noproc,nosysfs,nodevtmpfs
	sync
	echo 'Powering off'
	command poweroff -f
	sleep 1
	echo o > /proc/sysrq-trigger
	sleep 1
}

shutdown() {
	poweroff
}

halt() {
	poweroff
}
