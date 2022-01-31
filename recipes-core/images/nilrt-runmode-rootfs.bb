DESCRIPTION = "NI Linux RT runmode rootfs archive"

SRC_URI += "\
	file://bootimage.ini \
"

IMAGE_INSTALL = "\
	packagegroup-ni-runmode \
	packagegroup-ni-wifi \
	dkms \
	nilrt-grub-runmode \
	"

require includes/nilrt-image-base.inc
require includes/nilrt-xfce.inc


# INITRAMFS #

INITRAMFS_IMAGE = "nilrt-runmode-initramfs"

do_rootfs[depends] += "${INITRAMFS_IMAGE}:do_image_complete"

install_initramfs() {
	install -d ${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}
	install -m 0644 ${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE}-${MACHINE}.cpio.gz \
		${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/runmode_initramfs.gz
}

ROOTFS_POSTPROCESS_COMMAND += "install_initramfs;"


# ROOTFS #

require includes/licenses.inc
require includes/nilrt-proprietary.inc

IMAGE_INSTALL_NODEPS += "\
	${NI_PROPRIETARY_COMMON_PACKAGES} \
	${NI_PROPRIETARY_RUNMODE_PACKAGES} \
"

COPY_LIC_DIRS_SKIP_PKGS = "\
        ${NI_PROPRIETARY_COMMON_PACKAGES} \
	${NI_PROPRIETARY_RUNMODE_PACKAGES} \
        ni-sysdetails-webservice \
"

# Ensure that rauc does not end up in this image.
PACKAGE_EXCLUDE += "rauc rauc-mark-good"

# on older NILRT distro flavors the kernel is installed in non-standard paths
# for backward compatibility
CUSTOM_KERNEL_PATH ?= "/boot/tmp/runmode"

bootimg_fixup() {
	install -m 0644 "${THISDIR}/files/bootimage.ini" "${IMAGE_ROOTFS}/boot/runmode/bootimage.ini"
	sed -i "s/%component_version%/${BUILDNAME}/" "${IMAGE_ROOTFS}/boot/runmode/bootimage.ini"

	# Postinst script is going to want this all in /boot/tmp/runmode
	install -d `dirname "${IMAGE_ROOTFS}/${CUSTOM_KERNEL_PATH}"`
	mv "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}" "${IMAGE_ROOTFS}/${CUSTOM_KERNEL_PATH}"
}

IMAGE_PREPROCESS_COMMAND += " bootimg_fixup; "

IMAGE_FSTYPES += "squashfs tar.gz"
