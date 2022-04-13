SUMMARY = "Initscript for autloading NI modules"
DESCRIPTION = "Initscript to autoload NI modules in /etc/modules.autoload.d"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = "\
	file://ni-modules-autoload \
"

FILES_${PN} = "\
	${sysconfdir}/modules.autoload.d \
	${sysconfdir}/init.d/ni-modules-autoload \
"

RDEPENDS_${PN} += "bash"

INITSCRIPT_NAME = "ni-modules-autoload"
INITSCRIPT_PARAMS = "start 37 S ."

inherit update-rc.d

do_install () {
	install -d ${D}${sysconfdir}/modules.autoload.d

	install -d ${D}${sysconfdir}/init.d/
	install -m 0755 ${WORKDIR}/ni-modules-autoload ${D}${sysconfdir}/init.d/
}
