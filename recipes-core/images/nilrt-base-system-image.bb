DESCRIPTION = "NI Linux RT base system image archive and CDF"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

IMAGE_FSTYPES = "tar"
IMAGE_NAME_SUFFIX = ""

SRC_URI += " \
    file://${BPN}.postinst \
    file://${BPN}.cdf \
"

PV = "${DISTRO_VERSION}"

CDFGUID = "4C0005F7-54D1-492B-A7E7-C1E58BD9B972"

ROOTFS_IMAGE = "nilrt-runmode-rootfs"
do_rootfs[depends] += "${ROOTFS_IMAGE}:do_image_complete"

bootimg_fixup() {
	install -m 0644 "${DEPLOY_DIR_IMAGE}/${ROOTFS_IMAGE}-${MACHINE}.tar.gz" "${IMAGE_ROOTFS}/data.tar.gz"
	install -m 0755 "${THISDIR}/files/${BPN}.postinst" "${IMAGE_ROOTFS}/postinst"

	# Remove everything that is not data.tar.gz nor the postinst
	find "${IMAGE_ROOTFS}" -mindepth 1 \
		-not -path "${IMAGE_ROOTFS}/data.tar.gz" \
		-a -not -path "${IMAGE_ROOTFS}/postinst" \
		-delete
}

# TODO: We're doing CDF generation here. The CDF encodes the filename of the
#       .tar. However, we're not generating it with the same name that we
#       used to ("systemlink-linux-x64-dkms.tar"); is having these artifacts
#       with a new name fine?
create_cdf() {
	CDFOUT="${DEPLOY_DIR_IMAGE}/${IMAGE_BASENAME}-${MACHINE}${IMAGE_NAME_SUFFIX}.cdf"
	install -m 0644 "${THISDIR}/files/${BPN}.cdf" $CDFOUT

	GUID="{${CDFGUID}}"
	SHORTVER=$(echo ${BUILDNAME} | sed 's/^\([0-9.]*\).*/\1/;')
	TARFILE="${IMAGE_BASENAME}-${MACHINE}${IMAGE_NAME_SUFFIX}.tar"

	sed -i "s/%guid%/$GUID/g; s/%version%/$SHORTVER/g; s/%filename%/$TARFILE/g;" $CDFOUT
}

IMAGE_PREPROCESS_COMMAND += "bootimg_fixup;"
IMAGE_POSTPROCESS_COMMAND += "create_cdf;"

inherit image
