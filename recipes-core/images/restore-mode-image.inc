DESCRIPTION ?= "Tiny initramfs image intended to run restore mode operations"
LICENSE = "MIT"

FACTORY_IMAGE ?= "minimal-nilrt-image"

PACKAGE_INSTALL = "${ROOTFS_BOOTSTRAP_INSTALL} \
                   packagegroup-ni-restoremode \
"

DEPENDS += "init-restore-mode minimal-nilrt-image"
DEPENDS_xilinx-zynqhf += "zynq-bootscripts zynq-itb"
do_rootfs[depends] += "${FACTORY_IMAGE}:do_image_tar"

# Remove any kernel-image that the kernel-module-* packages may have pulled in.
PACKAGE_REMOVE = "kernel-image-*"

prune_unused_packages() {
	opkg -o ${IMAGE_ROOTFS} -f ${IPKGCONF_TARGET} --force-depends remove ${PACKAGE_REMOVE};
}

install_payload () {
	install -d ${IMAGE_ROOTFS}/payload
	install -m 0644 ${DEPLOY_DIR_IMAGE}/${FACTORY_IMAGE}-${MACHINE}.tar.bz2  ${IMAGE_ROOTFS}/payload/factory-image.tar.bz2
}

install_payload_append_xilinx-zynqhf(){
	install -m 0644 ${PKG_CONFIG_SYSROOT_DIR}/boot/boot.scr  ${IMAGE_ROOTFS}/payload/
	install -m 0644 ${PKG_CONFIG_SYSROOT_DIR}/boot/default.scr  ${IMAGE_ROOTFS}/payload/
	install -m 0644 ${PKG_CONFIG_SYSROOT_DIR}/boot/safemode.scr  ${IMAGE_ROOTFS}/payload/
	install -m 0644 ${PKG_CONFIG_SYSROOT_DIR}/boot/linux_next_runmode.itb ${IMAGE_ROOTFS}/payload/
}

ROOTFS_POSTPROCESS_COMMAND += "prune_unused_packages; install_payload; "

IMAGE_PREPROCESS_COMMAND = "rootfs_update_timestamp;"

# Do not pollute the initrd image with rootfs features
IMAGE_FEATURES = ""

IMAGE_LINGUAS = ""

IMAGE_FSTYPES = "${INITRAMFS_FSTYPES}"
IMAGE_FSTYPES_xilinx-zynqhf += "cpio.gz.u-boot"
inherit core-image

BAD_RECOMMENDATIONS += "busybox-syslog"
