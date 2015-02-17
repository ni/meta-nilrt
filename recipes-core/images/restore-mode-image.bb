DESCRIPTION = "Tiny initramfs image intended to run restore mode operations"
LICENSE = "MIT"

PACKAGE_INSTALL = "init-restore-mode busybox base-passwd ${ROOTFS_BOOTSTRAP_INSTALL} \
                   util-linux-mount util-linux-umount kernel-module-atkbd \
"

# Remove any kernel-image that the kernel-module-* packages may have pulled in.
PACKAGE_REMOVE = "kernel-image-*"

prune_unused_packages() {
	opkg-cl -o ${IMAGE_ROOTFS} -f ${IPKGCONF_TARGET} --force-depends remove ${PACKAGE_REMOVE};
}
ROOTFS_POSTPROCESS_COMMAND += "prune_unused_packages; "

IMAGE_PREPROCESS_COMMAND = "rootfs_update_timestamp;"

# Do not pollute the initrd image with rootfs features
IMAGE_FEATURES = ""

IMAGE_LINGUAS = ""

IMAGE_FSTYPES = "${INITRAMFS_FSTYPES}"
inherit core-image

BAD_RECOMMENDATIONS += "busybox-syslog"
