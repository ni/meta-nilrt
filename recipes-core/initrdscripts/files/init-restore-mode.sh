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

early_setup
provision
exec sh
