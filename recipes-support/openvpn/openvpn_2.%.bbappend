FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "\
	file://99_openvpn \
	file://vpn \
	file://vpnctl \
"

DEPENDS += "shadow-native pseudo-native niacctbase"

RDEPENDS_{PN} += "niacctbase"

inherit update-rc.d

INITSCRIPT_NAME = "vpn"
INITSCRIPT_PARAMS = "start 42 4 5 . stop 5 0 1 2 6 ."

do_install_append () {
	install -d ${D}${sysconfdir}/default/volatiles/
	install -p -m 644 ${WORKDIR}/99_openvpn ${D}${sysconfdir}/default/volatiles/
	install -p -m 755 ${WORKDIR}/vpn ${D}${sysconfdir}/init.d/
	install -p -m 755 ${WORKDIR}/vpnctl ${D}${sbindir}/
}
