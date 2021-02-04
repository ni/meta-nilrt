SUMMARY = "Shutdown/reboot guard utility for NILRT"
DESCRIPTION = "Utility to prevent shutdown/reboot to protect critical operations"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = "\
	file://holdoff-shutdown \
	file://rguard \
"

FILES_${PN} += "\
	${sysconfdir}/init.d/holdoff-shutdown \
	${sbindir}/rguard \
"

RDEPENDS_${PN} += "bash"

INITSCRIPT_NAME = "holdoff-shutdown"
INITSCRIPT_PARAMS = "stop 00 0 6 ."

inherit update-rc.d

do_install () {
	install -d ${D}${sbindir}
	install -d ${D}${sysconfdir}/init.d
	install -d ${D}${sysconfdir}/holdoff-shutdown.d

	install -m 0755   ${WORKDIR}/holdoff-shutdown    ${D}${sysconfdir}/init.d
	install -m 0755   ${WORKDIR}/rguard              ${D}${sbindir}
}
