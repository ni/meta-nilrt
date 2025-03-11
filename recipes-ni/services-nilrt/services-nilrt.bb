SUMMARY = "SystemD nilrt Services"
DESCRIPTION = "nilrt distro-specific services to provide basic system functionality."
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

DEPENDS += "shadow-native pseudo-native niacctbase"

SRC_URI = "\
"

SYSTEMD_SERVICE:${PN} = "\
"

S = "${WORKDIR}"

FILES:${PN} += " \
"

RDEPENDS:${PN} += "\
	bash \
	niacctbase \
"
