DESCRIPTION = "NI Watchdog Petter"
SECTION = "base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit update-rc.d

S = "${WORKDIR}"

SRC_URI = "file://LICENSE \
	   file://niwatchdogpet.c \
	   file://niwatchdogpet.sh"

INITSCRIPT_NAME = "niwatchdogpet"
INITSCRIPT_PARAMS = "start 03 S ."

CFLAGS_append = " -std=c89 -Wall -Werror -pedantic"

do_compile() {
	${CC} -Os ${CFLAGS} ${WORKDIR}/niwatchdogpet.c -o niwatchdogpet ${LDFLAGS}
}

do_install() {
	install -m 0755 -d ${D}${sbindir} ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/niwatchdogpet ${D}${sbindir}
	install -m 0755 ${S}/niwatchdogpet.sh ${D}${sysconfdir}/init.d/niwatchdogpet
}
