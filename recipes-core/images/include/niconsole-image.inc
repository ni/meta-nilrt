
inherit core-image

ROOTFS_POSTPROCESS_COMMAND += "remove_unused_feeds; prune_unused_packages; "

remove_unused_feeds() {
	rm -f ${IMAGE_ROOTFS}/var/lib/opkg/lists/*
}

# Remove any kernel packages that may have pulled in.
PACKAGE_REMOVE = "kernel-*"

prune_unused_packages() {
	opkg-cl -o ${IMAGE_ROOTFS} -f ${IPKGCONF_TARGET} --force-depends remove ${PACKAGE_REMOVE}
}

IMAGE_PREPROCESS_COMMAND += "remove_kernel; "

remove_kernel() {
	rm -rf ${IMAGE_ROOTFS}/boot/*
	rm -f ${IMAGE_ROOTFS}/usr/lib/opkg/alternatives/bzImage
	rm -f ${IMAGE_ROOTFS}/usr/lib/opkg/alternatives/zImage
}

# without package-management update-rc.d gets removed from image
IMAGE_FEATURES += "package-management"
