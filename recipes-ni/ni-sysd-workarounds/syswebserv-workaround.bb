SUMMARY = "systemWebServer systemD workaround files"
DESCRIPTION = "Workaround for systemWebServer systemD implementation"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = " \
	file://syswebserv_workaround.service \
	file://run_natinst.conf \
"

DEPENDS += " \
	update-rc.d-native \
	shadow-native \
	pseudo-native \
"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = "syswebserv_workaround.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

# Create syswebserv_workaround.service and install to /usr/lib/systemd/system
FILES:${PN} += " \
	${systemd_system_unitdir}/syswebserv_workaround.service \
	${sysconfdir}/tmpfiles.d/run_natinst.conf \
"

S = "${WORKDIR}"

do_install () {
	install -d ${D}${systemd_system_unitdir}
	install -m 0644 ${S}/syswebserv_workaround.service ${D}${systemd_system_unitdir}

	install -d ${D}${sysconfdir}/tmpfiles.d/
	install -m 0755 ${S}/run_natinst.conf ${D}/etc/tmpfiles.d/
}

pkg_preinst_on_target:${PN} () {
	/etc/init.d/systemWebServer stop
	/sbin/update-rc.d -f systemWebServer remove
}

pkg_postinst_ontarget:${PN} () {
	chmod 775 /var/local/natinst
	chown lvuser:ni /var/local/natinst

	chmod 775 /var/local/natinst/log
}

RDEPENDS:${PN} += " \
	bash \
"
