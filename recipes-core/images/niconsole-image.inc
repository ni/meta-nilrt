# remove kernel on non nilrt-nxg because the kernel it's built outside OE
IMAGE_PREPROCESS_COMMAND += "\
	${@base_conditional('DISTRO', 'nilrt-nxg', '', 'remove_kernel;', d)} \
"

remove_kernel() {
	rm -rf ${IMAGE_ROOTFS}/boot/*
	rm -f ${IMAGE_ROOTFS}/usr/lib/opkg/alternatives/bzImage
	rm -f ${IMAGE_ROOTFS}/usr/lib/opkg/alternatives/zImage
}

# useradd and groupadd need to be on sysroot
do_rootfs[depends] += "shadow-native:do_populate_sysroot"

# kernel recipe requires depmodwrapper to populate modules.dep
do_rootfs[depends] += "depmodwrapper-cross:do_populate_sysroot"

# without package-management update-rc.d gets removed from image
IMAGE_FEATURES += "package-management"

# Grant some space for package installs after boot
IMAGE_ROOTFS_EXTRA_SPACE = "500000"

require nilrt-image-common.inc

addtask image_build_test before do_rootfs

do_image_build_test() {
	if ! echo ${IMAGE_FEATURES} | grep -q "package-management";then
		exit 1
	fi
}
