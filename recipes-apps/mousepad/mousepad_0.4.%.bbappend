FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/files:${THISDIR}/${PN}-${PV}:"

DEPENDS += "shadow-native pseudo-native niacctbase"
RDEPENDS_${PN} += " niacctbase"
do_install[depends] += "niacctbase:do_populate_sysroot"

SRC_URI += "file://mimeapps.list"

FILES_${PN} += "${localdir}/share/applications/mimeapps.list"

homedir = "/home/${LVRT_USER}"
localdir = "${homedir}/.local"

do_install_append () {
	install -d ${D}${localdir}/share/applications

	install -m 0644 ${WORKDIR}/mimeapps.list ${D}${localdir}/share/applications/

	chown -R ${LVRT_USER}:${LVRT_GROUP} ${D}${homedir}
}
