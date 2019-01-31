# This image is bootable to a console with package management support

# useradd and groupadd need to be on sysroot
do_rootfs[depends] += "shadow-native:do_populate_sysroot"

# kernel recipe requires depmodwrapper to populate modules.dep
do_rootfs[depends] += "depmodwrapper-cross:do_populate_sysroot"

# without package-management update-rc.d gets removed from image
IMAGE_FEATURES += "package-management"

# Grant some space for package installs after boot
IMAGE_ROOTFS_EXTRA_SPACE = "500000"

IMAGE_INSTALL += "\
	packagegroup-ni-base \
	packagegroup-ni-tzdata \
"

addtask image_build_test before do_rootfs

# We always want package-management support in this image, fail if not enabled
do_image_build_test() {
	if ! echo ${IMAGE_FEATURES} | grep -q "package-management";then
		exit 1
	fi
}

ROOTFS_POSTPROCESS_COMMAND += "\
	${@oe.utils.conditional('DISTRO', 'nilrt-nxg', '', 'install_dtbs;', d)} \
"

install_dtbs() {
	if [ "${ARCH}" = "arm" ]; then
		for f in ${DEPLOY_DIR_IMAGE}/ni-*.dtb; do
			install -m 0644 "$f" "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/$(basename $f)"
		done
	fi
}

# on older NILRT distro flavors the kernel is installed in non-standard paths
# for backward compatibility
CUSTOM_KERNEL_PATH ?= "/boot/tmp/runmode/"

require nilrt-image-common.inc
