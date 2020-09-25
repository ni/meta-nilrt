DESCRIPTION ?= "Tiny initramfs image intended to run restore mode operations"
LICENSE = "MIT"

NIBOOT_BUNDLE_IMAGE ?= ""

PACKAGE_INSTALL = "${ROOTFS_BOOTSTRAP_INSTALL} \
                   packagegroup-ni-restoremode \
"

IMAGE_FEATURES += "empty-root-password"

DEPENDS += "init-restore-mode"

VIRTUAL-RUNTIME_mountpoint = "util-linux-mountpoint"
PREFERRED_PROVIDER_getopt = "util-linux-getopt"
VIRTUAL-RUNTIME_getopt = "util-linux-getopt"
VIRTUAL-RUNTIME_base-utils = "util-linux"
PREFERRED_PROVIDER_virtual/base-utils="util-linux"

do_rootfs[depends] += "${NIBOOT_BUNDLE_IMAGE}:do_deploy"

# If BUILDNAME is not already set in the build environment (common when doing a local dev build)
# then set a default value that can be used only for display purposes. Setting the default value here
# limits the scope of the default value to this file.
BUILDNAME ?= "0.0.0"

install_payload () {
	install -d ${IMAGE_ROOTFS}/payload
	install -m 0644 ${DEPLOY_DIR_IMAGE}/${NIBOOT_BUNDLE_IMAGE}-${MACHINE}.raucb  ${IMAGE_ROOTFS}/payload/niboot.raucb
	echo "BUILD_IDENTIFIER=${BUILDNAME}" > ${IMAGE_ROOTFS}/payload/imageinfo
	echo "IMAGE_DISPLAY_NAME=${IMAGE_DISPLAY_NAME}" >> ${IMAGE_ROOTFS}/payload/imageinfo
}

symlink_iso () {
        ln -sf ${PN}-${MACHINE}.wic ${DEPLOY_DIR_IMAGE}/${PN}-${MACHINE}.iso
}

ROOTFS_POSTPROCESS_COMMAND += "symlink_iso;install_payload;"

require nilrt-image-common.inc
