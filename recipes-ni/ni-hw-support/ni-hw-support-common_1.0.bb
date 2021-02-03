SUMMARY = "Support scripts and utilities common to all NI hardware products"
DESCRIPTION = "Support scripts and utilities for all NI hardware products which are supported by NI LinuxRT."
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

DEPENDS += "\
	update-rc.d-native \
"

SRC_URI += "\
	file://init.d/ni-rename-ifaces \
"

S = "${WORKDIR}"


do_install () {
	install -d ${D}${sysconfdir}/init.d/

	install -m 0755 ${S}/init.d/ni-rename-ifaces ${D}${sysconfdir}/init.d/ni-rename-ifaces
}

pkg_postinst_${PN} () {
	if [ -n "$D" ]; then
		OPT="-r $D"
	else
		OPT="-s"
	fi

	update-rc.d $OPT ni-rename-ifaces start 38 S .
}

pkg_postrm_${PN} () {
	if [ -n "$D" ]; then
		OPT="-f -r $D"
	else
		OPT="-f"
	fi

	update-rc.d -f ni-rename-ifaces remove
}


PACKAGE_ARCH = "all"
PACKAGES_remove += "${PN}-staticdev ${PN}-dev ${PN}-dbg"

FILES_${PN} += "\
	${sysconfdir}/init.d/ni-rename-ifaces \
"

RDEPENDS_${PN} += "\
	bash \
"
