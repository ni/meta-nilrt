SUMMARY = "NI Linux Real-Time factory reset utility"
DESCRIPTION = "Installs the ni-factory-reset administrative utility"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = "file://ni-factory-reset"

S = "${UNPACKDIR}"

RDEPENDS:${PN} += "bash shadow"

do_install () {
	install -d ${D}${sbindir}
	install -m 0700 ${S}/ni-factory-reset ${D}${sbindir}/ni-factory-reset
}
