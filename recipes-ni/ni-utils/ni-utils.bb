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
	file://nisetbootmode.service \
	file://nisetled.service \
	file://nisetprimarymac.service \
"

script_location = "${@bb.utils.contains('INIT_MANAGER', 'sysvinit', '${sysconfdir}/init.d', '${sbindir}',d)}"
FILES:${PN} += "\
	${bindir}/status_led \
	${libdir}/nisetbootmode.functions \
	${script_location}/nisetbootmode \
	${script_location}/nisetled \
	${script_location}/nisetprimarymac \
	${sysconfdir}/natinst/networking/functions.common \
	/usr/local/natinst/bin/status_led \
	${@bb.utils.contains('INIT_MANAGER', 'systemd', '${systemd_unitdir}/system/nisetbootmode.service', '',d)} \
	${@bb.utils.contains('INIT_MANAGER', 'systemd', '${systemd_unitdir}/system/nisetled.service', '',d)} \
	${@bb.utils.contains('INIT_MANAGER', 'systemd', '${systemd_unitdir}/system/nisetprimarymac.service', '',d)} \
"

DEPENDS += "shadow-native pseudo-native niacctbase update-rc.d-native"

RDEPENDS:${PN} += "niacctbase bash"

RDEPENDS:${PN}:append:x64 = " fw-printenv"

S = "${WORKDIR}"

#systemd
inherit systemd
SYSTEMD_SERVICE:${PN} = " \
	nisetbootmode.service \
	nisetled.service \
	nisetprimarymac.service \
"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

do_install () {
	install -d ${D}${bindir}
	install -d ${D}${script_location}
	install -d ${D}${libdir}
	install -d ${D}${sysconfdir}/natinst/networking

	install -m 0755   ${WORKDIR}/status_led                  ${D}${bindir}
	install -m 0440   ${WORKDIR}/nisetbootmode.functions     ${D}${libdir}
	install -m 0550   ${WORKDIR}/nisetbootmode               ${D}${script_location}
	install -m 0755   ${WORKDIR}/nisetled                    ${D}${script_location}
	install -m 0755   ${WORKDIR}/nisetprimarymac             ${D}${script_location}
	install -m 0755   ${WORKDIR}/functions.common            ${D}${sysconfdir}/natinst/networking


	if ${@bb.utils.contains('INIT_MANAGER', 'sysvinit', 'true', 'false',d)}; then
		update-rc.d -r ${D} nisetled              start 40 S .
		update-rc.d -r ${D} nisetbootmode         start 80 S . stop 0 0 6 .
		update-rc.d -r ${D} nisetprimarymac       start 4 5 .
	else
		install -d ${D}${systemd_unitdir}/system
		install -d ${D}${sbindir}

		install -m 0644   ${WORKDIR}/nisetbootmode.service       ${D}${systemd_unitdir}/system
		install -m 0644   ${WORKDIR}/nisetled.service            ${D}${systemd_unitdir}/system
		install -m 0644   ${WORKDIR}/nisetprimarymac.service     ${D}${systemd_unitdir}/system
	fi

	chown 0:${LVRT_GROUP} ${D}${bindir}/status_led
	chown 0:${LVRT_GROUP} ${D}${script_location}/nisetbootmode

	# legacy symlink location
	install -d ${D}/usr/local/natinst/bin
	ln -sf ${bindir}/status_led ${D}/usr/local/natinst/bin/status_led
}

# Create symlinks to the previous location for compatibility
pkg_postinst_ontarget:${PN} () {
	if [ -e ${sbindir}/nisetbootmode ]; then
		ln -sf ${sbindir}/nisetbootmode ${sysconfdir}/init.d/nisetbootmode
	fi
}

pkg_prerm_ontarget:${PN} () {
	if [ -L ${sysconfdir}/init.d/nisetbootmode ]; then
		rm -f ${sysconfdir}/init.d/nisetbootmode
	fi
}
