FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

DEPENDS += "shadow-native pseudo-native niacctbase"

RDEPENDS_${PN} += "niacctbase"

do_install_append() {
	chmod 4550 ${D}${base_sbindir}/halt
	chown 0:${LVRT_GROUP} ${D}${base_sbindir}/halt
}
