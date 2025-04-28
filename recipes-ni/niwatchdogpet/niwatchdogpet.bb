DESCRIPTION = "NI Watchdog Petter"
SECTION = "base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit update-rc.d systemd

S = "${WORKDIR}"

SRC_URI = "\
	file://LICENSE \
	file://niwatchdogpet.c \
	file://niwatchdogpet.sh \
	file://niwatchdogpet.service \
"

INITSCRIPT_NAME = "niwatchdogpet"
INITSCRIPT_PARAMS = "start 05 S ."
SYSTEMD_SERVICE:${PN} = "niwatchdogpet.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

CFLAGS:append = " -std=c89 -Wall -Werror -pedantic"

do_compile() {
	${CC} -Os ${CFLAGS} ${WORKDIR}/niwatchdogpet.c -o niwatchdogpet ${LDFLAGS}
}

script_location = "${@bb.utils.contains('INIT_MANAGER','sysvinit','${sysconfdir}/init.d','${systemd_unitdir}/scripts',d)}"
FILES:${PN} += "\
	${sbindir}/niwatchdogpet \
	${script_location}/niwatchdogpet \
	${@bb.utils.contains('INIT_MANAGER','systemd','${systemd_system_unitdir}/niwatchdogpet.service','',d)} \
"

do_install() {
	install -m 0755 -d ${D}${sbindir} ${D}${script_location}
	install -m 0755 ${S}/niwatchdogpet ${D}${sbindir}
	install -m 0755 ${S}/niwatchdogpet.sh ${D}${script_location}/niwatchdogpet

	if ${@bb.utils.contains('INIT_MANAGER','systemd','true','false',d)}; then
		install -d ${D}${systemd_system_unitdir}
		install -m 0644 ${WORKDIR}/niwatchdogpet.service ${D}${systemd_system_unitdir}/
	fi
}

RDEPENDS:${PN} = "fw-printenv"
