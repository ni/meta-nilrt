SUMMARY = "Shutdown/reboot guard utility for NILRT"
DESCRIPTION = "Utility to prevent shutdown/reboot to protect critical operations"
HOMEPAGE = "https://github.com/ni/meta-nilrt"
SECTION = "base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "2.0"


SRC_URI = "\
	file://holdoff-shutdown \
	file://nilrt-safemode \
	file://rguard \
"

inherit update-rc.d
INITSCRIPT_NAME = "holdoff-shutdown"
INITSCRIPT_PARAMS = "stop 00 0 6 ."


do_install () {
	install -d ${D}${sbindir}
	install -m 0755 ${WORKDIR}/rguard ${D}${sbindir}

	install -d ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/holdoff-shutdown ${D}${sysconfdir}/init.d

	install -d ${D}${sysconfdir}/holdoff-shutdown.d
	install -m 0644 ${WORKDIR}/nilrt-safemode ${D}${sysconfdir}/holdoff-shutdown.d
}


PACKAGES += "${PN}-safemode"

FILES:${PN} = "\
	${sysconfdir}/init.d/holdoff-shutdown \
	${sbindir}/rguard \
"
RDEPENDS:${PN} += "bash"

# ni-shutdown-guard-safemode
SUMMARY:${PN}-safemode = "Shutdown/reboot guard run-parts file(s) for NILRT safemode"
DESCRIPTION:${PN}-safemode = "Run-parts file(s) for ni-shutdown-guard to prevent shutdown/reboot to protect critical operations in safemode"
FILES:${PN}-safemode = "${sysconfdir}/holdoff-shutdown.d/nilrt-safemode"
RDEPENDS:${PN}-safemode += "${PN}"
