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

# If BUILDNAME is not already set in the build environment (common when doing a local dev build)
# then set a default value that can be used only for display purposes. Setting the default value here
# limits the scope of the default value to this file.
BUILDNAME ?= "0.0.0"

create_payload_dir () {
   install -d ${IMAGE_ROOTFS}/payload
}

symlink_iso () {
        ln -sf ${IMAGE_BASENAME}-${MACHINE}.wic ${DEPLOY_DIR_IMAGE}/${IMAGE_BASENAME}-${MACHINE}.iso
}

ROOTFS_POSTPROCESS_COMMAND += "create_payload_dir;symlink_iso;"

require nilrt-image-common.inc
