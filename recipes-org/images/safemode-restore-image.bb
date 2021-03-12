DESCRIPTION = "Tiny initramfs image intended to run restore mode operations for old NILinux RT, uses safemode-image"

IMAGE_FSTYPES = "${INITRAMFS_FSTYPES} tar.bz2 wic"

PACKAGE_INSTALL = "${ROOTFS_BOOTSTRAP_INSTALL} \
                   packagegroup-ni-restoremode \
                   safemode-image \
"

IMAGE_FEATURES += "empty-root-password"

DEPENDS += "init-restore-mode"

INITRAMFS_MAXSIZE = "524288"

VIRTUAL-RUNTIME_mountpoint = "util-linux-mountpoint"
PREFERRED_PROVIDER_getopt = "util-linux-getopt"
VIRTUAL-RUNTIME_getopt = "util-linux-getopt"
VIRTUAL-RUNTIME_base-utils = "util-linux"
PREFERRED_PROVIDER_virtual/base-utils="util-linux"

do_rootfs[depends] += "safemode-image:do_package_write_ipk"

symlink_iso () {
        ln -sf ${PN}-${MACHINE}.wic ${DEPLOY_DIR_IMAGE}/${PN}-${MACHINE}.iso
}

ROOTFS_POSTPROCESS_COMMAND += "symlink_iso;"

inherit core-image
