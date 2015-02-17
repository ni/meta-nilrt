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

early_setup
exec sh
