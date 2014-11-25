
inherit core-image

ROOTFS_POSTPROCESS_COMMAND += "remove_unused_feeds; "

remove_unused_feeds() {
	rm -f ${IMAGE_ROOTFS}/var/lib/opkg/lists/*
}

# useradd and groupadd need to be on sysroot
do_rootfs[depends] += "shadow-native:do_populate_sysroot"

# kernel recipe requires depmodwrapper to populate modules.dep
do_rootfs[depends] += "depmodwrapper-cross:do_populate_sysroot"

# without package-management update-rc.d gets removed from image
IMAGE_FEATURES += "package-management"
