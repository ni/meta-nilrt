DESCRIPTION = "Runmode image for ethernet-based NI Linux Real-Time targets running XFCE (DKMS)."

IMAGE_INSTALL = "\
	packagegroup-ni-runmode \
	packagegroup-ni-wifi \
	dkms \
	nilrt-grub-runmode \
	"

require niconsole-image.inc
require nilrt-xfce.inc
require nilrt-initramfs.inc
require include/licenses.inc
require nilrt-proprietary.inc

IMAGE_INSTALL_NODEPS += "\
	${NI_PROPRIETARY_BASE_PACKAGES} \
	${NI_PROPRIETARY_RUNMODE_PACKAGES} \
"

# Ensure that rauc does not end up in this image.
PACKAGE_EXCLUDE += "rauc rauc-mark-good"

bootimg_fixup() {
	# Postinst script is going to want this all in /boot/tmp/runmode
	install -d `dirname "${IMAGE_ROOTFS}/${CUSTOM_KERNEL_PATH}"`
	mv "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}" "${IMAGE_ROOTFS}/${CUSTOM_KERNEL_PATH}"
}

IMAGE_PREPROCESS_COMMAND += " bootimg_fixup; "

IMAGE_FSTYPES += "squashfs tar.gz"
