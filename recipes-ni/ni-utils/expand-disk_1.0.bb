SUMMARY = "Expand Disk for wic images"
DESCRIPTION = "This script will expand out the rootfs partition for wic images"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
LICENSE = "MIT"
SECTION = "base"

PACKAGES = "${PN}"

RDEPENDS_${PN} = "	\
					parted \
					e2fsprogs-resize2fs \
					bash \
"


SRC_URI = "	\
			file://S99expand_disk \
"

S = "${WORKDIR}"


do_install() {
# Since this is a one-off script, install directly to the rcS folder
	install -d ${D}${sysconfdir}/rcS.d
	install -m 0755    ${WORKDIR}/S99expand_disk	${D}${sysconfdir}/rcS.d/
}

