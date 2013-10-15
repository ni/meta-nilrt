FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

hostname = ""

SRC_URI += "file://natinst-path.sh"
SRC_URI += "file://safemode-ps1.sh"

do_install_append () {
	mkdir -p ${D}${sysconfdir}/profile.d/
	install ${WORKDIR}/natinst-path.sh ${D}${sysconfdir}/profile.d/
	install ${WORKDIR}/safemode-ps1.sh ${D}${sysconfdir}/profile.d/
}
