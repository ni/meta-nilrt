#!/bin/bash

ARCH=`uname -m`

PATH=/sbin:/bin:/usr/sbin:/usr/bin

early_setup() {
    mkdir -p /proc
    mkdir -p /sys
    mkdir -p /run/lock
    mount -t proc proc /proc
    mount -t sysfs sysfs /sys
    mount -t devtmpfs none /dev
}

provision() {
    #TODO: Find another solution to make sure all bring up messages are printed
    # before starting the provisioning script
    sleep 2
    ./ni_provisioning
}

# Removes the /boot/bootmode file that may force
# grub to boot into restore over and over again.
remove_bootmode() {
    NILRT_MOUNT_POINT=/mnt
    if mount -L nilrt $NILRT_MOUNT_POINT; then
        rm $NILRT_MOUNT_POINT/boot/bootmode
        umount $NILRT_MOUNT_POINT
    fi
}

# HACK: BIOS enables cstates when they should be disabled and this makes the
# processor frequency go bonkers on crio's 903x dual-core (not 9034) affecting
# performance of the restore-mode's rootfs unpacking. We disable all cstates
# except C0 in all cpu cores until we get a BIOS update with cstates disabled
disable_x64_cstates() {
    shopt -s nullglob
    for CSTATE_DISABLE in /sys/devices/system/cpu/cpu*/cpuidle/state[^0]/disable; do
        echo 1 > $CSTATE_DISABLE
    done
}


if [[ $ARCH == "x86_64" ]];then
    early_setup
    disable_x64_cstates
    # support VMWare image keyboard
    modprobe atkbd 2> /dev/null
    remove_bootmode 2> /dev/null
    provision
    while true;do
        echo ""
        echo "To reboot the system execute the command \"reboot -f\""
        echo ""
        /sbin/getty -l sh -n 38400 tty0
    done
elif [[ $ARCH =~ "arm" ]];then
    early_setup
    #fw_printenv/setenv requires the existence of /run/lock to create the lock file
    provision
    while true;do
        echo ""
        echo "To reboot the system execute the command \"reboot -f\""
        echo ""
        /sbin/getty -l sh -n 115200 ttyS0
    done
else
    echo ""
    echo "ERROR: Restoring system architecture '$ARCH' is not supported. Rebooting in 10 9 8..."
    echo ""
    sleep 10
    reboot -f
fi
