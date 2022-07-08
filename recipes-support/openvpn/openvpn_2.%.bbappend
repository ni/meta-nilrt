FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "\
	file://init.d/vpn \
	file://volatile \
	file://vpnctl \
"

INITSCRIPT_NAME:${PN} = "vpn"
INITSCRIPT_PARAMS:${PN} = "start 42 4 5 . stop 5 0 1 2 6 ."


do_install:append () {
	install -d ${D}${sysconfdir}/default/volatiles/
	install --mode=0644 ${WORKDIR}/volatile ${D}${sysconfdir}/default/volatiles/99_${PN}

	install -d ${D}${sysconfdir}/init.d
	install --mode=0755 ${WORKDIR}/init.d/vpn ${D}${sysconfdir}/init.d/

	install --mode=0755 ${WORKDIR}/vpnctl ${D}${sbindir}/
}
