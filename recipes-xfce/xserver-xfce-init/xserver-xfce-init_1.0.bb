SUMMARY = "NILRT XFCE Initialization."
DESCRIPTION = "Used to initialize XFCE when the embedded UI is enabled."
SECTION = "x11"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=751419260aa954499f7abaabaa882bbe"


SRC_URI = " \
	file://xserver-xfce \
	file://gplv2-license.patch \
	file://xserver-xfce.service \
	file://xserver-xfce.conf \
	file://xserver-logrotate.conf \
"

S = "${WORKDIR}"


inherit allarch update-rc.d
INITSCRIPT_NAME = "xserver-xfce"
INITSCRIPT_PARAMS = "start 01 5 2 . stop 01 0 1 6 ."
INITSCRIPT_PARAMS_shr = "start 90 5 2 . stop 90 0 1 6 ."
SYSTEMD_SERVICE:${PN} = "xserver-xfce.service"


do_install() {
	if ${@bb.utils.contains('DISTRO_FEATURES','sysvinit','true','false',d)}; then
		install -d ${D}${sysconfdir}/init.d
		install -d ${D}${sysconfdir}/default/
		install xserver-xfce ${D}${sysconfdir}/init.d
		install -m 0644 xserver-xfce.conf ${D}${sysconfdir}/default/xserver-xfce
	fi
	if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
		install -d ${D}${systemd_unitdir}/system
		install -m 0644 xserver-xfce.conf ${D}${sysconfdir}/default/xserver-xfce
		install -m 0644 ${WORKDIR}/xserver-xfce.service ${D}${systemd_unitdir}/system
	fi
	install -d ${D}${sysconfdir}/logrotate.d
	install -m 0644 xserver-logrotate.conf ${D}${sysconfdir}/logrotate.d/xserver.conf
}


FILES_${PN} += "${sysconfdir}/default/xserver-xfce"
# Get util-linux for su
RDEPENDS:${PN} = "xserver-common (>= 1.30) xinit xfce4-session util-linux"
RCONFLICTS:${PN} = "xserver-nodm-init"
