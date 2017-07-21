SUMMARY = "System configuration files"
DESCRIPTION = "Configuration files for the National Instruments System Configuration subsystem."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
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

user = "${LVRT_USER}"
group = "${LVRT_GROUP}"

do_install () {
	# UIXML config (soft dip switches, etc.)
	install -d -m 0755 ${D}/usr/local/natinst/share/uixml/sysconfig/
	install -m 0644 ${WORKDIR}/uixml/* ${D}/usr/local/natinst/share/uixml/sysconfig/

	# Common interface for system settings (soft dip switches, etc.)
	install -d -m 0775 ${D}${localstatedir}/local/natinst/systemsettings/
	chown ${user}:${group} ${D}${localstatedir}/local/natinst/systemsettings/
	install -m 0644 ${WORKDIR}/systemsettings/* ${D}${localstatedir}/local/natinst/systemsettings/

	install -d -m 0755 ${D}${sysconfdir}/natinst/
	install -m 0755 ${WORKDIR}/niselectsystemsettings ${D}${sysconfdir}/natinst/
}

# To delay the execution of the postinst to first boot, check $D and error
# if empty. Process explained in the Yocto Manual Post-Installation Scripts
# section.
pkg_postinst_${PN} () {
	if [ x"$D" = "x" ]; then
		${sysconfdir}/natinst/niselectsystemsettings postinst
	else
		exit 1
	fi
}
