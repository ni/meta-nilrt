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
	${@oe.utils.conditional('DISTRO', 'nilrt-nxg', '', 'install_dtbs;', d)} \
"

# TODO: remove once OE-core decides to symlink python3 as /usr/bin/python instead of 2.
symlink_python() {
	if [ -f ${IMAGE_ROOTFS}/${bindir}/python3 ]; then
		ln -sf ${bindir}/python3 ${IMAGE_ROOTFS}/${bindir}/python
	fi
}

install_dtbs() {
	if [ "${ARCH}" == "arm" ]; then
		for f in ${DEPLOY_DIR_IMAGE}/ni-*.dtb; do
			install -m 0644 "$f" "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/$(basename $f)"
		done
	fi
}

# older NILRT modules are versioned using two squashfs images, one with headers,
# the other with tools such as gcc, make, etc. NXG images don't use these.
do_rootfs[depends] += "\
	module-tools:do_image_complete \
	module-headers:do_image_complete \
"

# rootfs destination names don't make much sense but keep them to avoid breaking
# backwards compatibility on older NILRT (tools are expecting these paths).
install_module_versioning_squashfs() {
	TOOLS_IMAGE="${PREFERRED_PROVIDER_virtual/kernel}-module-tools"
	HEADERS_IMAGE="${PREFERRED_PROVIDER_virtual/kernel}-module-headers"

	install -d ${IMAGE_ROOTFS}/usr/local/natinst/tools

	install -m 0644 ${DEPLOY_DIR_IMAGE}/${TOOLS_IMAGE}-${MACHINE}.squashfs \
		${IMAGE_ROOTFS}/usr/local/natinst/tools/tools-squashfs-image.squashfs
	install -m 0644 ${DEPLOY_DIR_IMAGE}/${HEADERS_IMAGE}-${MACHINE}.squashfs \
		${IMAGE_ROOTFS}/usr/local/natinst/tools/module-versioning-image.squashfs
}

require nilrt-image-common.inc
