DESCRIPTION = "Tiny initramfs image intended to run recovery and install operations for NILinux RT"

IMAGE_FSTYPES = "${INITRAMFS_FSTYPES} tar.bz2 wic"

PACKAGE_INSTALL = "${ROOTFS_BOOTSTRAP_INSTALL} \
                   packagegroup-ni-restoremode \
"

IMAGE_FEATURES += "empty-root-password"

DEPENDS += "init-restore-mode"

INITRAMFS_MAXSIZE = "524288"

VIRTUAL-RUNTIME_mountpoint = "util-linux-mountpoint"
PREFERRED_PROVIDER_getopt = "util-linux-getopt"
VIRTUAL-RUNTIME_getopt = "util-linux-getopt"
VIRTUAL-RUNTIME_base-utils = "util-linux"
PREFERRED_PROVIDER_virtual/base-utils="util-linux"

SRC_URI += "\
	file://grubenv_non_ni_target \
	file://unicode.pf2 \
"

SAFEMODE_IMAGE = "nilrt-safemode-image"
do_rootfs[depends] += "${SAFEMODE_IMAGE}:do_image_complete"

bootimg_fixup() {
	install -d ${IMAGE_ROOTFS}/payload/fonts

	tar -xf "${DEPLOY_DIR_IMAGE}/${SAFEMODE_IMAGE}-${MACHINE}.tar.gz" -C ${IMAGE_ROOTFS}/payload

	install -m 0644 ${THISDIR}/files/grubenv_non_ni_target	${IMAGE_ROOTFS}/payload
	install -m 0644 ${THISDIR}/files/unicode.pf2		${IMAGE_ROOTFS}/payload/fonts

	GRUB_VERSION=$(echo ${GRUB_BRANCH} | cut -d "/" -f 2)

	echo "BUILD_IDENTIFIER=${BUILD_IDENTIFIER}" > ${IMAGE_ROOTFS}/payload/imageinfo
	echo "GRUB_VERSION=${GRUB_VERSION}.0" >> ${IMAGE_ROOTFS}/payload/imageinfo
}

symlink_iso () {
	ln -sf ${PN}-${MACHINE}.wic ${DEPLOY_DIR_IMAGE}/${PN}-${MACHINE}.iso
}

ROOTFS_POSTPROCESS_COMMAND += "symlink_iso;"

IMAGE_PREPROCESS_COMMAND += " bootimg_fixup; "

inherit core-image
