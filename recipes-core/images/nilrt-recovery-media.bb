DESCRIPTION = "NI Linux RT installation/recovery media ISO"

IMAGE_FSTYPES:append = " wic"

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

SAFEMODE_IMAGE = "nilrt-safemode-rootfs"
do_rootfs[depends] += "${SAFEMODE_IMAGE}:do_image_complete"

bootimg_fixup() {
	install -d ${IMAGE_ROOTFS}/payload/fonts

	tar -xf "${DEPLOY_DIR_IMAGE}/${SAFEMODE_IMAGE}-${MACHINE}.tar.gz" -C ${IMAGE_ROOTFS}/payload

	install -m 0644 ${THISDIR}/files/grubenv_non_ni_target	${IMAGE_ROOTFS}/payload
	install -m 0644 ${THISDIR}/files/unicode.pf2		${IMAGE_ROOTFS}/payload/fonts

	echo "BUILD_IDENTIFIER=${BUILD_IDENTIFIER}" > ${IMAGE_ROOTFS}/payload/imageinfo
}

symlink_iso () {
	ln -sf ${PN}-${MACHINE}.rootfs.wic ${DEPLOY_DIR_IMAGE}/${PN}-${MACHINE}.iso
}

ROOTFS_POSTPROCESS_COMMAND += "symlink_iso;"

IMAGE_PREPROCESS_COMMAND += " bootimg_fixup; "

# Use the same kernel binary as the image to create the wic boot device
WIC_CREATE_EXTRA_ARGS = "-k ${IMAGE_ROOTFS}/boot/runmode"

require includes/nilrt-core-image.inc
