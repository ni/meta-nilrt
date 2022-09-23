FILESEXTRAPATHS:prepend := "${THISDIR}:${THISDIR}/files:${THISDIR}/${PN}-${PV}:"

DEPENDS += "shadow-native pseudo-native niacctbase"
RDEPENDS:${PN} += " niacctbase"
do_install[depends] += "niacctbase:do_populate_sysroot"

SRC_URI += "file://mimeapps.list"

FILES:${PN} += "${localdir}/share/applications/mimeapps.list"

homedir = "/home/${LVRT_USER}"
localdir = "${homedir}/.local"

do_install:append () {
	install -d ${D}${localdir}/share/applications

	install -m 0644 ${WORKDIR}/mimeapps.list ${D}${localdir}/share/applications/

	chown -R ${LVRT_USER}:${LVRT_GROUP} ${D}${homedir}
}
