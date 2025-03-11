SUMMARY = "SystemD nilrt services for safemode"
DESCRIPTION = "nilrt distro-specific services to provide basic system functionality."
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

DEPENDS += "shadow-native pseudo-native niacctbase"

RDEPENDS:${PN} += "bash niacctbase"

SRC_URI = " \
"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"
SYSTEMD_SERVICE:${PN} = " \
"

FILES:${PN} += " \
"

S = "${WORKDIR}"

REQUIRED_DISTRO_FEATURES = " systemd"

RDEPENDS:${PN} += "\
	bash \
	niacctbase \
"
