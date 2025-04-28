SUMMARY = "Initscript for autloading NI modules"
DESCRIPTION = "Initscript to autoload NI modules in /etc/modules.autoload.d"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = "\
	file://ni-modules-autoload \
	file://ni-modules-autoload.service \
"

script_location = "${@bb.utils.contains('INIT_MANAGER','sysvinit','${sysconfdir}/init.d','${systemd_unitdir}/scripts',d)}"
FILES:${PN} += " \
	${sysconfdir}/modules.autoload.d \
	${script_location}/ni-modules-autoload \
	${@bb.utils.contains('INIT_MANAGER','systemd','${systemd_system_unitdir}/ni-modules-autoload.service','',d)} \
"

RDEPENDS:${PN} += "bash"

INITSCRIPT_NAME = "ni-modules-autoload"
INITSCRIPT_PARAMS = "start 37 S ."
SYSTEMD_SERVICE:${PN} = "ni-modules-autoload.service"
SYSTEMD_AUTO_ENABLE:{PN} = "enable"

inherit update-rc.d systemd

do_install () {
	install -d ${D}${sysconfdir}/modules.autoload.d

	install -d ${D}${script_location}
	install -m 0755 ${WORKDIR}/ni-modules-autoload ${D}${script_location}

	if ${@bb.utils.contains('INIT_MANAGER','systemd','true','false',d)}; then
		install -d ${D}${systemd_system_unitdir}
		install -m 0644 ${WORKDIR}/ni-modules-autoload.service ${D}${systemd_system_unitdir}/
	fi
}
