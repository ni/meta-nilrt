FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

DEPENDS += "shadow-native pseudo-native niacctbase"

RDEPENDS_${PN} += "niacctbase"

group = "${LVRT_GROUP}"

do_install_append() {
	chmod 4550 ${D}${base_sbindir}/halt
	chown 0:${group} ${D}${base_sbindir}/halt
}

