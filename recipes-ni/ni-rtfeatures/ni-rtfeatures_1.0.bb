SUMMARY = "rtfeatures user-space tools"
DESCRIPTION = "Provides user-space tools to support the nirtfeatures kernel module."
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

DEPENDS += "\
	update-rc.d-native \
"

SRC_URI += "\
	file://init.d/handle_cpld_ip_reset \
	file://rtfeatures.rules \
"

S = "${WORKDIR}"


do_install () {
	install -d ${D}${sysconfdir}/init.d/

	install -m 0755 ${S}/init.d/handle_cpld_ip_reset    ${D}${sysconfdir}/init.d

	install -d ${D}${sysconfdir}/udev/rules.d

	install -m 0644 ${S}/rtfeatures.rules    ${D}${sysconfdir}/udev/rules.d/rtfeatures.rules
}

pkg_postinst_${PN} () {
	if [ -n "$D" ]; then
		OPT="-r $D"
	else
		OPT="-s"
	fi

	update-rc.d $OPT handle_cpld_ip_reset    start 6 1 3 4 5 .
}

pkg_postrm_${PN} () {
	if [ -n "$D" ]; then
		OPT="-f -r $D"
	else
		OPT="-f"
	fi

	update-rc.d $OPT handle_cpld_ip_reset  remove
}


PACKAGE_ARCH = "all"
PACKAGES_remove += "${PN}-staticdev ${PN}-dev ${PN}-dbg"

FILES_${PN} += "\
	${sysconfdir}/init.d/handle_cpld_ip_reset \
	${sysconfdir}/udev \
"

RDEPENDS_${PN} += "\
	bash \
	ni-netcfgutil \
	udev \
"
