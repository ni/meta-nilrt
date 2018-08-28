SUMMARY = "Miscellaneous nilrt utilities"
DESCRIPTION = "nilrt distro-specific miscellaneous utilities that provide basic system functionality."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = "file://status_led \
	   file://nisetbootmode.functions \
	   file://nisetbootmode \
"

FILES_${PN} += "\
	/usr/lib/nisetbootmode.functions \
"

DEPENDS += "shadow-native pseudo-native niacctbase"

RDEPENDS_${PN} += "niacctbase bash"

RDEPENDS_${PN}_append_x64 += "fw-printenv"

S = "${WORKDIR}"

do_install () {
	install -d ${D}${bindir}
	install -d ${D}${sysconfdir}
	install -d ${D}${libdir}
	install -m 0755   ${WORKDIR}/status_led         ${D}${bindir}
	install -m 0440   ${WORKDIR}/nisetbootmode.functions         ${D}${libdir}
	install -m 0550   ${WORKDIR}/nisetbootmode         ${D}${bindir}

	chown 0:${LVRT_GROUP} ${D}${bindir}/status_led

}
