SUMMARY = "CompactRIO support files"
DESCRIPTION = "CompactRIO miscellaneous support files"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"
DEPENDS = " \
	shadow-native \
	pseudo-native \
	niacctbase \
	${@bb.utils.contains('INIT_MANAGER','sysvinit','update-rc.d-native','systemd-systemctl-native',d)} \
"
RDEPENDS:${PN} += "niacctbase bash"

SRC_URI:append:x64 = " \
	file://nisetfpgaautoload \
	file://nisetfpgaautoload.service \
	file://nisetconsoleout \
	file://nisetconsoleout.service \
"

S = "${WORKDIR}"

inherit systemd
SYSTEMD_SERVICE:${PN} = " \
	nisetfpgaautoload.service \
	nisetconsoleout.service \
"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

script_location = "${@bb.utils.contains('INIT_MANAGER','sysvinit','${sysconfdir}/init.d','${systemd_unitdir}/scripts',d)}"
FILES:${PN} = " \
	${script_location}/nisetfpgaautoload \
	${script_location}/nisetconsoleout \
	${@bb.utils.contains('INIT_MANAGER','systemd','${systemd_system_unitdir}/nisetfpgaautoload.service','',d)} \
	${@bb.utils.contains('INIT_MANAGER','systemd','${systemd_system_unitdir}/nisetconsoleout.service','',d)} \
"
do_install () {
	install -d ${D}${script_location}
	if [ "${TARGET_ARCH}" = "x86_64" ]; then
		install -m 0755   ${S}/nisetfpgaautoload    ${D}${script_location}
		install -m 0550   ${S}/nisetconsoleout      ${D}${script_location}
		chown 0:${LVRT_GROUP} ${D}${script_location}/nisetconsoleout

		if ${@bb.utils.contains('INIT_MANAGER','sysvinit','true','false',d)}; then
			update-rc.d -r ${D} nisetfpgaautoload start 81 S . stop 3 0 6 .
			update-rc.d -r ${D} nisetconsoleout start 15 S . stop 85 0 6 .
		fi
		if ${@bb.utils.contains('INIT_MANAGER','systemd','true','false',d)}; then
			install -d ${D}${systemd_system_unitdir}
			install -m 0644 ${WORKDIR}/nisetfpgaautoload.service ${D}${systemd_system_unitdir}/
			install -m 0644 ${WORKDIR}/nisetconsoleout.service ${D}${systemd_system_unitdir}/
		fi
	fi
}
