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

CDFGUID:x64 = "4C0005F7-54D1-492B-A7E7-C1E58BD9B972"
# TODO: Update GUID to E40D0BD6-F888-4BDB-AFB9-EF97A34107B3 when ARM scarthgap is ready to ship
CDFGUID:xilinx-zynq = "8E3EACD0-B36E-462B-A500-88AE644AB3B0"

OSVALUE:x64 = "NI-Linux x64"
OSVALUE:xilinx-zynq = "Linux-ARMv7-A"

OSVERSION:x64 = "7.0"
# For BSI to fit on smaller ARM targets, safemode needs zlib compression and other space saving measures.
# So set minimum compatible safemode version to 26.0 which has them.
OSVERSION:xilinx-zynq = "26.0"

ROOTFS_IMAGE = "nilrt-runmode-rootfs"
do_rootfs[depends] += "${ROOTFS_IMAGE}:do_image_complete"

bootimg_fixup() {
	install -m 0644 "${DEPLOY_DIR_IMAGE}/${ROOTFS_IMAGE}-${MACHINE}.rootfs.tar.gz" "${IMAGE_ROOTFS}/data.tar.gz"
	install -m 0755 "${THISDIR}/files/${BPN}.postinst" "${IMAGE_ROOTFS}/postinst"

	# Remove everything that is not data.tar.gz nor the postinst
	find "${IMAGE_ROOTFS}" -mindepth 1 \
		-not -path "${IMAGE_ROOTFS}/data.tar.gz" \
		-a -not -path "${IMAGE_ROOTFS}/postinst" \
		-delete
}

create_cdf() {
	CDFOUT="${DEPLOY_DIR_IMAGE}/${IMAGE_BASENAME}-${MACHINE}${IMAGE_NAME_SUFFIX}.cdf"
	install -m 0644 "${THISDIR}/files/${BPN}.cdf" $CDFOUT

	GUID="{${CDFGUID}}"
	SHORTVER=$(echo ${BUILDNAME} | sed 's/^\([0-9.]*\).*/\1/;')
	TARFILE="${IMAGE_BASENAME}-${MACHINE}${IMAGE_NAME_SUFFIX}.tar"

	sed -i "s/%guid%/$GUID/g; s/%version%/$SHORTVER/g; s/%osvalue%/${OSVALUE}/g; s/%osversion%/${OSVERSION}/g; s/%filename%/$TARFILE/g;" $CDFOUT
}

IMAGE_PREPROCESS_COMMAND += "bootimg_fixup;"
IMAGE_POSTPROCESS_COMMAND += "create_cdf;"

inherit image
