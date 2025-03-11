SUMMARY = "SystemD nilrt Services"
DESCRIPTION = "nilrt distro-specific services to provide basic system functionality."
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

DEPENDS += "shadow-native pseudo-native niacctbase"

SRC_URI = "\
	file://cleanvarcache.service \
"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"
SYSTEMD_SERVICE:${PN} = "\
	cleanvarcache.service \
"

S = "${WORKDIR}"

do_install () {
	install -d ${D}${systemd_unitdir}/system

	install -m 0644 ${WORKDIR}/cleanvarcache.service ${D}${systemd_unitdir}/system
}

FILES:${PN} += " \
	${systemd_unitdir}/system/cleanvarcache.service \
"

RDEPENDS:${PN} += "\
	bash \
	niacctbase \
"
