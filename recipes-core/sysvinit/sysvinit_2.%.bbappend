FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

DEPENDS += "shadow-native pseudo-native niacctbase"

SRC_URI += "file://initscript"


do_install_append() {
	chmod 4550 ${D}${base_sbindir}/halt
	chown 0:${LVRT_GROUP} ${D}${base_sbindir}/halt

	install -d "${D}${sysconfdir}"
	install -m 0754 ${WORKDIR}/initscript "${D}${sysconfdir}/initscript"
}


RDEPENDS_${PN} += "niacctbase"
