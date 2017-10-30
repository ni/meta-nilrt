DESCRIPTION ?= "Base image setup used in all NILRT images"
LICENSE = "MIT"

IMAGE_FSTYPES = "${INITRAMFS_FSTYPES} tar.bz2"
IMAGE_FSTYPES_append_n310 += "wic"
INITRAMFS_FSTYPES_xilinx_zynqhf = "cpio.gz.u-boot"

PACKAGE_REMOVE = "kernel-*"

prune_unused_packages() {
	opkg -o ${IMAGE_ROOTFS} -f ${IPKGCONF_TARGET} --force-depends remove ${PACKAGE_REMOVE};
}

remove_unused_feeds() {
	rm -f ${IMAGE_ROOTFS}/var/lib/opkg/lists/*
}

ROOTFS_POSTPROCESS_COMMAND += "prune_unused_packages; remove_unused_feeds;"

IMAGE_PREPROCESS_COMMAND += "rootfs_update_timestamp;"

IMAGE_FEATURES ??= ""

IMAGE_LINGUAS ??= ""

inherit core-image
