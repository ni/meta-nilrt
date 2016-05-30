#!/bin/bash
COMMON_BOOTARGS='console=ttyS0,115200 $mtdparts ubi.mtd=boot-config ubi.mtd=root $othbootargs'
RESTORE_BOOTARGS="setenv bootargs $COMMON_BOOTARGS ramdisk_size=135168 root=/dev/ram rw"
DEFAULT_BOOTARGS="setenv bootargs $COMMON_BOOTARGS root=ubi1:rootfs rw rootfstype=ubifs threadsirqs=1 kthreadd_pri=25 ksoftirqd_pri=8 irqthread_pri=15 \${usb_gadget_args}"
BW_MIGRATE_BOOTARGS="setenv bootargs $COMMON_BOOTARGS ramdisk_size=135168 root=/dev/ram rw"

load_restore_boot()
{
    RESTORE_BOOTMODE=$1
RESTORE_BOOTCMD="
if ubi part boot-config &&
    ubifsmount ubi:bootfs &&
    ubifsload \$loadaddr .restore/restore.scr;
then
    restore=$RESTORE_BOOTMODE;
    source \$loadaddr;
else
    echo Restore mode error.;
fi;
"
    echo $RESTORE_BOOTCMD
}

DEFAULT_BOOTCMD="
if ubi part root &&
    ubifsmount ubi:rootfs &&
    ubifsload \$loadaddr /boot/uImage &&
    ubifsload \$verifyaddr /boot/ni-\$DeviceCode.dtb;
then
    $DEFAULT_BOOTARGS safe_mode=false;
    bootm \$loadaddr - \$verifyaddr;
elif
    ubifsload \$loadaddr /boot/safemode.scr;
then
    source \$loadaddr;
else
    "`load_restore_boot restore`"
fi;"


SAFEMODE_BOOTCMD="
if ubi part root &&
    ubifsmount ubi:rootfs &&
    ubifsload \$loadaddr /boot/uImage &&
    ubifsload \$verifyaddr /boot/ni-\$DeviceCode.dtb;
then
    $DEFAULT_BOOTARGS safe_mode=true;
    bootm \$loadaddr - \$verifyaddr;
else
    "`load_restore_boot restore`"
fi;"

RESTORE_BOOTCMD="
if ubifsload \$loadaddr .restore/uImage &&
    ubifsload 0x8500000 .restore/ni-\$DeviceCode.dtb &&
    ubifsload 0x8700000 .restore/ramdisk;
then
    $RESTORE_BOOTARGS
    restore=\$restore;
    bootm \$loadaddr 0x8700000 0x8500000;
else
    echo Restore mode error;
fi;"

CHECK_SAFEMODE='
i2c read 0x40 1 1 $verifyaddr;
setexpr.b cpld_safemode *$verifyaddr \& 0x01;
if test $cpld_safemode -eq 1;
then
    '"$SAFEMODE_BOOTCMD"'
fi;
'
BOOTCMD='
i2c read 0x40 0x1F 1 $verifyaddr;
setexpr.b next_bootmode *$verifyaddr \& 0x3;
if test $next_bootmode -eq 0;
then
    if ubifsload $loadaddr /boot/default.scr;
    then
        source $loadaddr;
    elif ubifsload $loadaddr /boot/safemode.scr;
    then
        source $loadaddr;
    else
        '`load_restore_boot restore`'
    fi;
elif test $next_bootmode -eq 2;
then
    '`load_restore_boot restore`'
elif test $next_bootmode -eq 3;
then
    '`load_restore_boot auto-restore`'
else
    echo FATAL ERROR!;
fi;
'

BW_MIGRATION_BOOTCMD="
if ubi part root &&
    ubifsmount ubi:rootfs &&
    ubifsload 0x8500000 boot/.oldNILinuxRT/uImage &&
    ubifsload 0x9000000 boot/.oldNILinuxRT/dtbs/ni-\$DeviceCode.dtb &&
    ubifsload 0x9200000 boot/.oldNILinuxRT/ramdisk;
then
    $BW_MIGRATE_BOOTARGS restore=backward-migrate;
    bootm 0x8500000 0x9200000 0x9000000;
else
    echo Migration image corrupt!;
fi;
"

# cRIO-9068 doesn't have USB gadget, so skip it on that device
USB_GADGET_ARGS='
if test ${DeviceCode} != 0x76D6;
then
    usb_gadget_args="g_ether.idVendor=${USBVendorID} g_ether.idProduct=${USBProductID} g_ether.iProduct=\"${USBProduct} [${hostname}]\" g_ether.iSerialNumber=${serial#} g_ether.dev_addr=${usbgadgetethaddr} g_ether.bcdDevice=${USBDevice}";
fi;'

echo $USB_GADGET_ARGS > top_level_bootscript
echo $CHECK_SAFEMODE >> top_level_bootscript
echo $BOOTCMD >> top_level_bootscript
echo $DEFAULT_BOOTCMD > default_bootscript
echo $SAFEMODE_BOOTCMD > safemode_bootscript
echo $RESTORE_BOOTCMD > restore_bootscript
echo $BW_MIGRATION_BOOTCMD > bw_migrate_bootscript
echo $BOOTCMD >> bw_migrate_bootscript
