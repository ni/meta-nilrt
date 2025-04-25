SUMMARY = "Shutdown/reboot guard utility for NILRT"
DESCRIPTION = "Utility to prevent shutdown/reboot to protect critical operations"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = "\
	file://holdoff-shutdown \
	file://holdoff-shutdown.service \
	file://nilrt-safemode \
	file://rguard \
"

script_location = "${@bb.utils.contains('INIT_MANAGER','sysvinit','${sysconfdir}/init.d','${systemd_unitdir}/scripts',d)}"
# ni-shutdown-guard package settings
FILES:${PN} = "\
	${script_location}/holdoff-shutdown \
	${sbindir}/rguard \
"

RDEPENDS:${PN} += "bash"

inherit update-rc.d systemd
INITSCRIPT_NAME = "holdoff-shutdown"
INITSCRIPT_PARAMS = "stop 00 0 6 ."
SYSTEMD_SERVICE:${PN} = "holdoff-shutdown.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

# ni-shutdown-guard-safemode package settings
PACKAGES += "${PN}-safemode"

SUMMARY:${PN}-safemode = "Shutdown/reboot guard run-parts file(s) for NILRT safemode"
DESCRIPTION:${PN}-safemode = "Run-parts file(s) for ni-shutdown-guard to prevent shutdown/reboot to protect critical operations in safemode"

FILES:${PN}-safemode = "\
	${sysconfdir}/holdoff-shutdown.d/nilrt-safemode \
"

RDEPENDS:${PN}-safemode += "${PN}"

do_install () {
	install -d ${D}${sbindir}
	install -d ${D}${script_location}
	install -d ${D}${sysconfdir}/holdoff-shutdown.d

	install -m 0755   ${WORKDIR}/rguard              ${D}${sbindir}
	install -m 0755   ${WORKDIR}/holdoff-shutdown    ${D}${script_location}
	install -m 0644   ${WORKDIR}/nilrt-safemode      ${D}${sysconfdir}/holdoff-shutdown.d

	if ${@bb.utils.contains('INIT_MANAGER','systemd','true','false',d)}; then
		install -d ${D}${systemd_system_unitdir}
		install -m 0644 ${WORKDIR}/holdoff-shutdown.service ${D}${systemd_system_unitdir}/
	fi

}
