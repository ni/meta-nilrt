DESCRIPTION ?= "Base image setup used in all NILRT images"
LICENSE = "MIT"

IMAGE_FSTYPES = "${INITRAMFS_FSTYPES} tar.bz2 ext2"

IMAGE_FEATURES ??= ""
IMAGE_LINGUAS ??= ""

IMAGE_PREPROCESS_COMMAND += "rootfs_update_timestamp;"

ROOTFS_POSTPROCESS_COMMAND += "\
	${@base_conditional('DISTRO', 'nilrt-nxg', '', 'prune_unused_packages; remove_kernel;', d)} \
	remove_unused_feeds; \
"

PACKAGE_REMOVE ??= "kernel-*"

prune_unused_packages() {
	opkg -o ${IMAGE_ROOTFS} -f ${IPKGCONF_TARGET} --force-depends remove ${PACKAGE_REMOVE};
}

remove_unused_feeds() {
	rm -f ${IMAGE_ROOTFS}/var/lib/opkg/lists/*
}

remove_kernel() {
	rm -rf ${IMAGE_ROOTFS}/boot/*
	rm -f ${IMAGE_ROOTFS}/usr/lib/opkg/alternatives/bzImage
	rm -f ${IMAGE_ROOTFS}/usr/lib/opkg/alternatives/zImage
}

inherit core-image
