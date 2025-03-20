SUMMARY = "NILRT systemd services"
DESCRIPTION = "nilrt modifications for services provided by systemd"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

DEPENDS += "systemd"

SRC_URI += " \
	file://nihostname.service \
	file://hostname.sh \
"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"
SYSTEMD_SERVICE:${PN}:append = " \
	nihostname.service \
"

S = "${WORKDIR}"

do_install() {
	install -d ${D}${systemd_unitdir}/system
	install -d ${D}${libdir}/systemd/scripts

	install -m 0644 ${WORKDIR}/nihostname.service ${D}${systemd_unitdir}/system/nihostname.service
	install -m 0755 ${WORKDIR}/hostname.sh ${D}${libdir}/systemd/scripts/hostname.sh
}

FILES:${PN}:append = " \
	${libdir}/systemd/scripts/hostname.sh \
"

RDEPENDS:${PN} += " \
	bash \
"
