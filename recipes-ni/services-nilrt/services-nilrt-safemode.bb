SUMMARY = "SystemD nilrt services for safemode"
DESCRIPTION = "nilrt distro-specific services to provide basic system functionality."
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

DEPENDS += "shadow-native pseudo-native niacctbase"

RDEPENDS:${PN} += "bash niacctbase"

SRC_URI = " \
	file://mountcompatibility.service \
	file://mountcompatibility \
"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"
SYSTEMD_SERVICE:${PN} = " \
	mountcompatibility.service \
"

FILES:${PN} += " \
	${systemd_unitdir}/system/mountcompatibility.service \
	${libdir}/systemd/scripts/mountcompatibility \
"

S = "${WORKDIR}"

do_install () {
	install -d ${D}${systemd_unitdir}/system
	install -d ${D}${libdir}/systemd/scripts

	install -m 0644 ${WORKDIR}/mountcompatibility.service ${D}${systemd_unitdir}/system
	install -m 0755 ${WORKDIR}/mountcompatibility ${D}${libdir}/systemd/scripts
}

REQUIRED_DISTRO_FEATURES = " systemd"

RDEPENDS:${PN} += "\
	bash \
	niacctbase \
"
