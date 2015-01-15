DESCRIPTION = "Runmode image for ethernet-based NI Linux Real-Time cRIO targets running XFCE."

IMAGE_PREPROCESS_COMMAND = "rootfs_update_timestamp; populate_bootloader_files;"

BOOTIMAGE_CFG_FILE =  "${IMAGE_ROOTFS}/boot/runmode/bootimage.cfg"
BOOTIMAGE_INI_FILE =  "${IMAGE_ROOTFS}/boot/runmode/bootimage.ini"

populate_bootloader_files() {
    # create bootimage.cfg
    cat > ${BOOTIMAGE_CFG_FILE} <<EOF
set consoleparam='console=tty0 console=ttyS0,115200n8'
set kernel_path='/runmode/bzImage'
set otherbootargs='rootwait rootfstype=ext4 rw usbcore.usbfs_memory_mb=0 consoleblank=0 threadsirqs=1 kthreadd_pri=25 ksoftirqd_pri=8 irqthread_pri=15'
set usb_gadget_args="g_ether.idVendor=\${USBVendorID} g_ether.idProduct=\${USBProductID} g_ether.iProduct=\${USBProduct}[\${hostname}] g_ether.iSerialNumber=\${SerialNum} g_ether.dev_addr=\${usbgadgetethaddr} g_ether.bcdDevice=\${USBDevice}"
EOF

    #create bootimage.ini
    cat > ${BOOTIMAGE_INI_FILE} <<EOF
[BootImageDetails]
device_code=0x7755 0x774B 0x7735 0x774D 0x773B 0x7773 0x76CE 0x77AA 0x77B9 0x77BC 0x77BD 0x77DB 0x77DC 0x77DD
EOF

}

IMAGE_INSTALL = "\
    packagegroup-ni-base \
    packagegroup-ni-tzdata \
    packagegroup-ni-runmode \
    packagegroup-ni-wifi \
    packagegroup-ni-xfce \
    packagegroup-ni-crio \
    ${ni_external_components} \
    "

IMAGE_FSTYPES = "tar.bz2"

require include/ni-external-components.inc

require include/niconsole-image.inc

IMAGE_FEATURES += "x11"
