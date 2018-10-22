DESCRIPTION = "Runmode image for ethernet-based NI Linux Real-Time targets running XFCE."

IMAGE_INSTALL = "\
	packagegroup-ni-base \
	packagegroup-ni-tzdata \
	packagegroup-ni-runmode \
	packagegroup-ni-wifi \
	packagegroup-ni-xfce \
	"

INITRAMFS_IMAGE = "niconsole-initramfs"

do_rootfs[depends] += "${INITRAMFS_IMAGE}:do_image_complete"

install_initramfs() {
	install -d ${IMAGE_ROOTFS}${CUSTOM_KERNEL_PATH}
	install -m 0644 ${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE}-${MACHINE}.cpio.gz \
		${IMAGE_ROOTFS}${CUSTOM_KERNEL_PATH}/runmode_initramfs.gz
}

ROOTFS_POSTPROCESS_COMMAND += "install_initramfs; install_module_versioning_squashfs;"

require niconsole-image.inc
require include/licenses.inc

IMAGE_FEATURES += "x11"
