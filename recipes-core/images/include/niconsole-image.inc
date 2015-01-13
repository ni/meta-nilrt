
inherit core-image

ROOTFS_POSTPROCESS_COMMAND += "remove_unused_feeds; "

remove_unused_feeds() {
	rm -f ${IMAGE_ROOTFS}/var/lib/opkg/lists/*
}

IMAGE_PREPROCESS_COMMAND += "remove_kernel; "

remove_kernel() {
	rm -rf ${IMAGE_ROOTFS}/boot/*
	rm -f ${IMAGE_ROOTFS}/usr/lib/opkg/alternatives/bzImage
	rm -f ${IMAGE_ROOTFS}/usr/lib/opkg/alternatives/zImage
}

# without package-management update-rc.d gets removed from image
IMAGE_FEATURES += "package-management"
