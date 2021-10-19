SUMMARY = "SysV nilrt init scripts for safemode"
DESCRIPTION = "nilrt distro-specific initscripts to provide basic system functionality."
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

DEPENDS += "shadow-native pseudo-native update-rc.d-native niacctbase"

RDEPENDS_${PN} += "bash niacctbase update-rc.d"

SRC_URI = " \
"

SRC_URI_append_x64 = " \
	file://nisafemodereason \
	file://niselectnetnaming \
"

S = "${WORKDIR}"

do_install () {
	install -d ${D}${sysconfdir}/init.d/
}

do_install_append_x64 () {
	install -m 0755   ${WORKDIR}/niselectnetnaming      ${D}${sysconfdir}/init.d
	update-rc.d -r ${D} niselectnetnaming start 3 S .

	install -m 0755   ${WORKDIR}/nisafemodereason       ${D}${sysconfdir}/init.d
	update-rc.d -r ${D} nisafemodereason start 60 S .
}
