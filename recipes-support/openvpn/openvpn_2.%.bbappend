FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "\
	file://volatile \
	file://vpn \
	file://vpnctl \
"

DEPENDS += "shadow-native pseudo-native niacctbase"

RDEPENDS_{PN} += "niacctbase"

INITSCRIPT_NAME_${PN} = "vpn"
INITSCRIPT_PARAMS_${PN} = "start 42 4 5 . stop 5 0 1 2 6 ."

do_install_append () {
	install -d ${D}${sysconfdir}/default/volatiles/
	install --mode=0644 ${WORKDIR}/volatile ${D}${sysconfdir}/default/volatiles/99_${PN}

	install -d ${D}${sysconfdir}/init.d
	install --mode=0755 ${WORKDIR}/vpn ${D}${sysconfdir}/init.d/

	install --mode=0755 ${WORKDIR}/vpnctl ${D}${sbindir}/
}
