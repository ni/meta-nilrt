# This image is bootable to a console with package management support

# useradd and groupadd need to be on sysroot
do_rootfs[depends] += "shadow-native:do_populate_sysroot"

# kernel recipe requires depmodwrapper to populate modules.dep
do_rootfs[depends] += "depmodwrapper-cross:do_populate_sysroot"

# without package-management update-rc.d gets removed from image
IMAGE_FEATURES += "package-management"

# Grant some space for package installs after boot
IMAGE_ROOTFS_EXTRA_SPACE = "500000"

addtask image_build_test before do_rootfs

# We always want package-management support in this image, fail if not enabled
do_image_build_test() {
	if ! echo ${IMAGE_FEATURES} | grep -q "package-management";then
		exit 1
	fi
}

ROOTFS_POSTPROCESS_COMMAND += "\
	symlink_python; \
"

# TODO: remove once OE-core decides to symlink python3 as /usr/bin/python instead of 2.
symlink_python() {
	if [ -f ${IMAGE_ROOTFS}/${bindir}/python3 ]; then
		ln -sf ${bindir}/python3 ${IMAGE_ROOTFS}/${bindir}/python
	fi
}

move_kernel_append_arm() {
	for f in ${DEPLOY_DIR_IMAGE}/ni-*.dtb; do
		install -m 0644 "$f" "${IMAGE_ROOTFS}/${CUSTOM_KERNEL_PATH}/$(basename $f)"
	done
}

require nilrt-image-common.inc
