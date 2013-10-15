FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

hostname = ""

SRC_URI += "file://natinst-path.sh"

do_install_append () {
	install ${WORKDIR}/natinst-path.sh ${D}${sysconfdir}/profile.d/
}
