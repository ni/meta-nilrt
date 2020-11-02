#!/bin/bash

ARCH=$(uname -m)

export PATH="/sbin:/bin:/usr/sbin:/usr/bin"

umask 0022

NIRECOVERY_MOUNTPOINT=/mnt/NIRECOVERY
MOUNT_NIRECOVERY_USB_TIME=10
BOOTMEDIA_UUID=

parse_args() {
   for x in $(cat /proc/cmdline); do
       case $x in
       bootmedia_uuid=*)
          BOOTMEDIA_UUID=${x#bootmedia_uuid=}
          ;;
       esac
   done
}

early_setup() {
    mkdir -p /proc
    mkdir -p /sys
    mkdir -p /run/lock
    mount -t proc proc /proc
    mount -t sysfs sysfs /sys
    mount -t efivarfs efivarfs /sys/firmware/efi/efivars
    mount -t devtmpfs none /dev

    parse_args

    COUNT=0
    while [ $COUNT -le "$MOUNT_NIRECOVERY_USB_TIME" ]; do
        mount_nirecovery_usb
        if mountpoint -q $NIRECOVERY_MOUNTPOINT; then
            break
        fi
        COUNT=$(expr $COUNT + 1)
        sleep 1
    done
    mount_payload

    # Set hostname
    echo "recovery" | tee /etc/hostname > /proc/sys/kernel/hostname
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

show_console() {
    while true; do
        echo ""
        echo " ------------------------------------------------------"
        echo " -  NI Linux Real-Time Recovery shell                 -"
        echo " ------------------------------------------------------"
        echo ""

        /usr/bin/setsid /sbin/getty 38400 console --noclear -a root --login-options "-p -- \u"

        sleep 1
    done
}

start_serial_console() {
    SERIAL_TTY="ttyS0"
    if [ -c /dev/${SERIAL_TTY} ]; then
        if [ $(cat /sys/devices/virtual/tty/console/active) != "${SERIAL_TTY}" ]; then
            while true; do
                /usr/bin/setsid /sbin/getty 115200 ${SERIAL_TTY} --noclear -a root --login-options "-p -- \u"

                sleep 1
            done
        fi
    fi
}

mount_nirecovery_usb()
{
    if ! mountpoint -q $NIRECOVERY_MOUNTPOINT; then
        if [ ! -d $NIRECOVERY_MOUNTPOINT ]; then
            mkdir -p $NIRECOVERY_MOUNTPOINT
        fi
        mount -o ro,sync,relatime UUID=$BOOTMEDIA_UUID $NIRECOVERY_MOUNTPOINT &> /dev/null
    fi
}

mount_payload()
{
   if [ -d $NIRECOVERY_MOUNTPOINT/payload ]; then
      # We are booting from USB so payload will be on same partition as kernel and initrd
      mount --bind $NIRECOVERY_MOUNTPOINT/payload /payload;
   else
      # We used migration ipk, so payload would be in nirootfs
      ROOTFS_MOUNTPOINT=$(mktemp -d "/var/volatile/rootfs-XXXXXXX")

      if [ -z  $ROOTFS_MOUNTPOINT ]
      then
         echo ""
         echo "ERROR: Failed to create temporary directory to mount nirootfs"
         echo ""

         sync
         show_console

         exit 1
      fi

      mount -o rw -L nirootfs $ROOTFS_MOUNTPOINT;

      # Delete everything except payload directory to ensure device
      # has enough space to extract payload
      for dir in $ROOTFS_MOUNTPOINT/*; do
          if [ ${dir##*/} != "payload" ]; then
              rm -rf $dir;
          fi
      done

      # Payload is in an ipk, extract it
      mkdir -p $ROOTFS_MOUNTPOINT/payload/extracted
      pushd $ROOTFS_MOUNTPOINT/payload/extracted > /dev/null

      ar x $ROOTFS_MOUNTPOINT/payload/*.ipk
      mkdir data
      tar -xpf data.tar* -C data

      # Ramdisk size is limited, so copy payload to a tmpfs
      mount -t tmpfs -o size=420m tmpfs /payload
      cp data/usr/share/nilrt/* /payload
      mv /payload/*.raucb /payload/niboot.raucb
      popd > /dev/null

      umount $ROOTFS_MOUNTPOINT;
   fi
}

early_setup

start_serial_console &

# Arch-specific set-up
if [[ $ARCH == "x86_64" ]]; then
    disable_x64_cstates
    # support VMWare image keyboard
    modprobe atkbd 2> /dev/null
    modprobe i8042 2> /dev/null
    remove_bootmode 2> /dev/null
fi

if [[ $ARCH =~ ^(x86_64|armv7l)$ ]]; then
    /ni_provisioning
else
    echo ""
    echo "ERROR: ARCH=$ARCH is not supported by provisioning tool."
    echo " You can try running /ni_provisioning manually from the shell."
    echo ""
fi

sync
show_console


# Uh oh. Something went wrong. We should never reach this point.
# Sync file systems and exit init (this process).

sync
exit 1
