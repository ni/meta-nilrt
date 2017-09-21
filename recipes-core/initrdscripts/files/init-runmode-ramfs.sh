#!/bin/sh
ARCH=`uname -m`

mkdir -p /proc
mkdir -p /sys
mkdir -p /dev
mount -t proc proc /proc
mount -t sysfs sysfs /sys
mount -t devtmpfs none /dev

if [[ $ARCH == "x86_64" ]]; then
    depmod -a
    mount -o ro "$rootuuid" /mnt/root
    exec switch_root /mnt/root /sbin/init
fi

echo ""
echo "ERROR: ARCH=$ARCH is not supported by this initramfs"
echo ""

umount -a -r
exit 1
