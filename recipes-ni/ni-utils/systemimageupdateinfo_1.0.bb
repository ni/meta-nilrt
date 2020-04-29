SUMMARY = "NI LinuxRT utility to determine the system's bootflow"
DESCRIPTION = "Provides a utility script which uses filesystem layout and system information to determine which National-Instruments-supported bootflow is in use"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = "file://systemimageupdateinfo"

RDEPENDS_${PN} += "bash"

S = "${WORKDIR}"

do_install () {
	install -d ${D}${bindir}
	install -m 0755   ${S}/systemimageupdateinfo         ${D}${bindir}
}
