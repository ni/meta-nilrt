SUMMARY = "NI persistent logs configuration utility"
DESCRIPTION = "Installs the ni-configpersistentlogs utility"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = "\
	file://ni-configpersistentlogs \
"

S = "${WORKDIR}"

INITSCRIPT_NAME = "ni-configpersistentlogs"
INITSCRIPT_PARAMS = "start 2 S ."

inherit update-rc.d

do_install () {
	install -d ${D}${sysconfdir}/init.d/
	install -m 0755 ${S}/ni-configpersistentlogs ${D}${sysconfdir}/init.d/
}


FILES_${PN} += "\
	${sysconfdir}/init.d/ni-configpersistentlogs \
"

RDEPENDS_${PN} += "bash initscripts"
