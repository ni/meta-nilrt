SUMMARY = "Safemode image for older nilrt"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "\
	file://grubenv_non_ni_target \
	file://unicode.pf2 \
"

RDEPENDS_${PN} += "grub-efi grub"
COMPATIBLE_MACHINE = "x64"

SAFEMODE_IMAGE = "nilrt-safemode-image"
do_rootfs[depends] += "${SAFEMODE_IMAGE}:do_image_complete"

do_install() {
	mkdir -p ${D}/payload/fonts

	tar -xf "${DEPLOY_DIR_IMAGE}/${SAFEMODE_IMAGE}-${MACHINE}.tar.gz" -C ${D}/payload

	cp ${WORKDIR}/grubenv_non_ni_target	${D}/payload
	cp ${WORKDIR}/unicode.pf2		${D}/payload/fonts

	GRUB_VERSION=$(echo ${GRUB_BRANCH} | cut -d "/" -f 2)

	echo "BUILD_IDENTIFIER=${BUILD_IDENTIFIER}" > ${D}/payload/imageinfo
	echo "GRUB_VERSION=${GRUB_VERSION}.0" >> ${D}/payload/imageinfo
}

FILES_${PN} = "\
	/payload \
"
