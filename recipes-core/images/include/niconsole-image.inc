
inherit core-image

ROOTFS_POSTPROCESS_COMMAND += "remove_unused_feeds; "

remove_unused_feeds() {
	rm -f ${IMAGE_ROOTFS}/var/lib/opkg/lists/*
}

# without package-management update-rc.d gets removed from image
IMAGE_FEATURES += "package-management"
