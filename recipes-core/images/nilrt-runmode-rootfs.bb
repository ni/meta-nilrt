DESCRIPTION = "NI Linux RT runmode rootfs archive"

SRC_URI += "\
	file://bootimage.ini \
"

IMAGE_INSTALL = "\
	packagegroup-ni-runmode \
	packagegroup-ni-wifi \
	dkms \
"

IMAGE_INSTALL:append:x64 = "\
	nilrt-grub-runmode \
	"

IMAGE_INSTALL:append:xilinx-zynq = "\
	linux-nilrt-fitimage \
	"

require includes/nilrt-image-base.inc
require includes/nilrt-xfce.inc
require includes/nilrt-proprietary.inc

IMAGE_INSTALL_NODEPS += "\
	${NI_PROPRIETARY_COMMON_PACKAGES} \
	${NI_PROPRIETARY_RUNMODE_PACKAGES} \
"

# Ensure that rauc does not end up in this image.
PACKAGE_EXCLUDE += "rauc rauc-mark-good"

# on older NILRT distro flavors the kernel is installed in non-standard paths
# for backward compatibility
CUSTOM_KERNEL_PATH:x64 ?= "/boot/tmp/runmode"

bootimg_fixup_x64() {
	install -m 0644 "${THISDIR}/files/bootimage.ini" "${IMAGE_ROOTFS}/boot/runmode/bootimage.ini"
	sed -i "s/%component_version%/${BUILDNAME}/" "${IMAGE_ROOTFS}/boot/runmode/bootimage.ini"

	# Postinst script is going to want this all in /boot/tmp/runmode
	install -d `dirname "${IMAGE_ROOTFS}/${CUSTOM_KERNEL_PATH}"`
	mv "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}" "${IMAGE_ROOTFS}/${CUSTOM_KERNEL_PATH}"
}

bootimg_fixup_arm() {
	mv "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/fitImage" "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/linux_runmode.itb"
}

IMAGE_PREPROCESS_COMMAND:append:x64 = " bootimg_fixup_x64; "
IMAGE_PREPROCESS_COMMAND:append:xilinx-zynq = " bootimg_fixup_arm; "

IMAGE_FSTYPES += "squashfs ${NILRT_BSI_FSTYPE}"
