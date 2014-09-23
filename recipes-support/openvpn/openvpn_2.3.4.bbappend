FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://vpn \
	    file://vpnctl \
	    file://99_openvpn \
"

DEPENDS += "niacctbase"

RDEPENDS_{PN} += "niacctbase"

inherit update-rc.d

group = "${OPENVPN_GROUP}"

INITSCRIPT_NAME = "vpn"
INITSCRIPT_PARAMS = "start 42 S . stop 5 0 1 2 6 ."

do_install_append () {
     install -d ${D}${base_bindir}
     install -d ${D}${sysconfdir}/default/volatiles/
     install -p -m 644 ${WORKDIR}/99_openvpn ${D}${sysconfdir}/default/volatiles/
     install -p -m 755 ${WORKDIR}/vpn ${D}${sysconfdir}/init.d/
     install -p -m 755 ${WORKDIR}/vpnctl ${D}${sbindir}/
}

pkg_postinst_${PN} () {
     mkdir -p $D${libdir}/iproute2-openvpn
     cp -f $D${base_sbindir}/ip.iproute2 $D${libdir}/iproute2-openvpn/ip
     chown root:${group} $D${libdir}/iproute2-openvpn/ip
     chmod 4750 $D${libdir}/iproute2-openvpn/ip
}

pkg_prerm_${PN}() {
     rm $D${libdir}/iproute2-openvpn/ip
}
