#!/bin/sh

PATH=/sbin:/bin:/usr/sbin:/usr/bin

early_setup() {
    mkdir -p /proc
    mkdir -p /sys
    mount -t proc proc /proc
    mount -t sysfs sysfs /sys
    mount -t devtmpfs none /dev

    # support VMWare image keyboard
    modprobe atkbd 2> /dev/null
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

early_setup
remove_bootmode 2> /dev/null
provision
while true;do
    echo ""
    echo "To reboot the system execute the command \"reboot -f\""
    echo ""
    /sbin/getty -l sh -n 38400 tty0
done
