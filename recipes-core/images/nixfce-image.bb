DESCRIPTION = "Runmode image for ethernet-based NI Linux Real-Time targets running XFCE."

IMAGE_PREPROCESS_COMMAND = "rootfs_update_timestamp;"

IMAGE_PREPROCESS_COMMAND_append_x64 = "populate_bootloader_files;"

populate_bootloader_files() {
	# create bootimage.cfg
	echo "set consoleparam='console=tty0 console=ttyS0,115200n8'" > ${IMAGE_ROOTFS}/boot/runmode/bootimage.cfg
	echo "set kernel_path='/runmode/bzImage" >> ${IMAGE_ROOTFS}/boot/runmode/bootimage.cfg
	echo "set otherbootargs='rootwait rootfstype=ext4 rw usbcore.usbfs_memory_mb=0 consoleblank=0 threadsirqs=1 kthreadd_pri=25 ksoftirqd_pri=8 irqthread_pri=15" >> ${IMAGE_ROOTFS}/boot/runmode/bootimage.cfg
	echo "set usb_gadget_args=\"g_ether.idVendor=\${USBVendorID} g_ether.idProduct=\${USBProductID} g_ether.iProduct=\${USBProduct}[\${hostname}] g_ether.iSerialNumber=\${SerialNum} g_ether.dev_addr=\${usbgadgetethaddr} g_ether.bcdDevice=\${USBDevice}" >> ${IMAGE_ROOTFS}/boot/runmode/bootimage.cfg

	#create bootimage.ini
	echo "[BootImageDetails] " > ${IMAGE_ROOTFS}/boot/runmode/bootimage.ini
	echo "device_code=0x7755 0x774B 0x7735 0x774D 0x773B 0x7773 0x76CE 0x77AA 0x77B9 0x77BC 0x77BD 0x77DB 0x77DC 0x77DD" >> ${IMAGE_ROOTFS}/boot/runmode/bootimage.ini
}

IMAGE_INSTALL = "\
	packagegroup-ni-base \
	packagegroup-ni-tzdata \
	packagegroup-ni-runmode \
	packagegroup-ni-wifi \
	packagegroup-ni-xfce \
	"

IMAGE_FSTYPES = "tar.bz2"

require include/niconsole-image.inc

IMAGE_FEATURES += "x11"
