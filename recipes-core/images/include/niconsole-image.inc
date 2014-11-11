
inherit core-image

ROOTFS_POSTPROCESS_COMMAND += "remove_unused_feeds; "

remove_unused_feeds() {
	rm -f ${IMAGE_ROOTFS}/var/lib/opkg/lists/*
}

IMAGE_PREPROCESS_COMMAND += "remove_kernel; "

remove_kernel() {
	rm -f ${IMAGE_ROOTFS}/boot/*
}

# without package-management update-rc.d gets removed from image
IMAGE_FEATURES += "package-management"
