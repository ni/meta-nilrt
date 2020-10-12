INITRAMFS_IMAGE = "niconsole-initramfs"

do_rootfs[depends] += "${INITRAMFS_IMAGE}:do_image_complete"

install_initramfs() {
	install -d ${IMAGE_ROOTFS}${CUSTOM_KERNEL_PATH}
	install -m 0644 ${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE}-${MACHINE}.cpio.gz \
		${IMAGE_ROOTFS}${CUSTOM_KERNEL_PATH}/runmode_initramfs.gz
}

ROOTFS_POSTPROCESS_COMMAND += "install_initramfs;"
