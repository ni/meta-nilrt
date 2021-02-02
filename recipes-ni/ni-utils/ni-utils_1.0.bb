SUMMARY = "Miscellaneous nilrt utilities"
DESCRIPTION = "nilrt distro-specific miscellaneous utilities that provide basic system functionality."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = "\
	file://status_led \
	file://nisetbootmode.functions \
	file://nisetbootmode \
	file://nisetled \
	file://nisetprimarymac \
	file://functions.common \
"

FILES_${PN} += "\
	${bindir}/status_led \
	${libdir}/nisetbootmode.functions \
	${sysconfdir}/init.d/nisetbootmode \
	${sysconfdir}/init.d/nisetled \
	${sysconfdir}/init.d/nisetprimarymac \
	${sysconfdir}/natinst/networking/functions.common \
"

DEPENDS += "shadow-native pseudo-native niacctbase update-rc.d-native"

RDEPENDS_${PN} += "niacctbase bash"

RDEPENDS_${PN}_append_x64 += "fw-printenv"

S = "${WORKDIR}"

do_install () {
	install -d ${D}${bindir}
	install -d ${D}${sysconfdir}/init.d
	install -d ${D}${libdir}
	install -d ${D}${sysconfdir}/natinst/networking

	install -m 0755   ${WORKDIR}/status_led                  ${D}${bindir}
	install -m 0440   ${WORKDIR}/nisetbootmode.functions     ${D}${libdir}
	install -m 0550   ${WORKDIR}/nisetbootmode               ${D}${sysconfdir}/init.d
	install -m 0755   ${WORKDIR}/nisetled                    ${D}${sysconfdir}/init.d
	install -m 0550   ${WORKDIR}/nisetprimarymac             ${D}${sysconfdir}/init.d
	install -m 0755   ${WORKDIR}/functions.common            ${D}${sysconfdir}/natinst/networking

	update-rc.d -r ${D} nisetled              start 40 S .
	update-rc.d -r ${D} nisetbootmode         start 80 S . stop 0 0 6 .
	update-rc.d -r ${D} nisetprimarymac       start 4 5 .

	chown 0:${LVRT_GROUP} ${D}${bindir}/status_led
	chown 0:${LVRT_GROUP} ${D}${sysconfdir}/init.d/nisetbootmode
	chown 0:${LVRT_GROUP} ${D}${sysconfdir}/init.d/nisetprimarymac
}
