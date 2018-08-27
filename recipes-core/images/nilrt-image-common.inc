DESCRIPTION ?= "Base image setup used in all NILRT images"
LICENSE = "MIT"

IMAGE_FSTYPES = "${INITRAMFS_FSTYPES} tar.bz2 ext2"

IMAGE_FEATURES ??= ""
IMAGE_LINGUAS ??= ""

IMAGE_PREPROCESS_COMMAND += "rootfs_update_timestamp;"

ROOTFS_POSTPROCESS_COMMAND += "\
	${@oe.utils.conditional('DISTRO', 'nilrt-nxg', '', 'move_kernel;', d)} \
	remove_unused_feeds; \
"

remove_unused_feeds() {
	rm -f ${IMAGE_ROOTFS}/var/lib/opkg/lists/*
}

# on older NILRT distro flavors the kernel is installed in non-standard paths
# for backwards compatibility
move_kernel() {
	if [ ! -z "${CUSTOM_KERNEL_PATH}" ]; then
		install -d ${IMAGE_ROOTFS}/${CUSTOM_KERNEL_PATH}

		mv ${IMAGE_ROOTFS}/boot/${KERNEL_IMAGETYPE}-* \
			${IMAGE_ROOTFS}/${CUSTOM_KERNEL_PATH}/${KERNEL_IMAGETYPE}
	fi

	rm -f ${IMAGE_ROOTFS}/boot/${KERNEL_IMAGETYPE}*
	rm -f ${IMAGE_ROOTFS}/usr/lib/opkg/alternatives/${KERNEL_IMAGETYPE}*
}

inherit core-image
