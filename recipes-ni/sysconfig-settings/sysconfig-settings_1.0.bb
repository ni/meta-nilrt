SUMMARY = "System configuration files"
DESCRIPTION = "Configuration files for the National Instruments System Configuration subsystem."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = "file://niselectsystemsettings \
	   file://uixml/* \
	   file://systemsettings/* \
"

FILES_${PN} += "/usr/local/natinst/share/uixml/sysconfig/* \
"

DEPENDS += "shadow-native pseudo-native niacctbase"
RDEPENDS_${PN} += "niacctbase"

do_install[depends] += "niacctbase:do_populate_sysroot"

S = "${WORKDIR}"

do_install () {
	# UIXML config (soft dip switches, etc.)
	install -d -m 0755 ${D}/usr/local/natinst/share/uixml/sysconfig/
	install -m 0644 ${WORKDIR}/uixml/* ${D}/usr/local/natinst/share/uixml/sysconfig/

	# Common interface for system settings (soft dip switches, etc.)
	install -d -m 0775 ${D}${localstatedir}/local/natinst/systemsettings/
	chown ${LVRT_USER}:${LVRT_GROUP} ${D}${localstatedir}/local/natinst/systemsettings/
	install -m 0644 ${WORKDIR}/systemsettings/* ${D}${localstatedir}/local/natinst/systemsettings/

	install -d -m 0755 ${D}${sysconfdir}/natinst/
	install -m 0755 ${WORKDIR}/niselectsystemsettings ${D}${sysconfdir}/natinst/
}

pkg_postinst_ontarget_${PN} () {
	${sysconfdir}/natinst/niselectsystemsettings postinst
}
