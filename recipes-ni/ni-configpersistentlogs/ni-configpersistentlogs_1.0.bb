SUMMARY = "NI persistent logs configuration utility"
DESCRIPTION = "Installs the niconfigpersistentlogs utility"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

DEPENDS += "shadow-native pseudo-native niacctbase update-rc.d-native"

SRC_URI = "\
	file://niconfigpersistentlogs \
"

S = "${WORKDIR}"

do_install () {
	install -d ${D}${sysconfdir}/init.d/
	install -m 0755 ${S}/niconfigpersistentlogs ${D}${sysconfdir}/init.d/

	update-rc.d -r ${D} niconfigpersistentlogs   start 2 S .
}


FILES_${PN} += "\
	${sysconfdir}/init.d/niconfigpersistentlogs \
"

RDEPENDS_${PN} += "bash"
