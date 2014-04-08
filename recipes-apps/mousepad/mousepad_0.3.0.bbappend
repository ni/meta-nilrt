
FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/files:${THISDIR}/${PN}-${PV}:"

DEPENDS += "niacctbase"

SRC_URI += "file://mimeapps.list"

FILES_${PN} += "${localdir}/share/applications/mimeapps.list"

user = "${LVRT_USER}"
group = "${LVRT_GROUP}"
homedir = "/home/${user}"
localdir = "${homedir}/.local"

do_install_append () {
	install -d ${D}${localdir}/share/applications

	install -m 0644 ${WORKDIR}/mimeapps.list ${D}${localdir}/share/applications/

	chown -R ${user}:${group} ${D}${homedir}
}
