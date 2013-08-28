DESCRIPTION = "Central Regulatory Domain Daemon"
SECTION = "network"
LICENSE = "ISC"
LIC_FILES_CHKSUM = "file://LICENSE;md5=403e16e3768619e3adac3449870302f3"

PR = "r1"
DEPENDS = "libnl"
RDEPENDS_${PN} = "crda"

inherit update-rc.d

S = "${WORKDIR}"

SRC_URI = "file://LICENSE \
	  file://log.c \
	  file://log.h \
	  file://crdd.c \
	  file://crdd.sh"

INITSCRIPT_NAME = "crdd.sh"
INITSCRIPT_PARAMS = "start 04 S ."

LDFLAGS_append = " -lnl-3"
CFLAGS_append = " -I${STAGING_INCDIR}/libnl3"

do_compile() {
        ${CC} -Os ${CFLAGS} ${WORKDIR}/log.c ${WORKDIR}/crdd.c -o crdd ${LDFLAGS}
}

do_install() {
	install -m 0755 -d ${D}${sbindir} ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/crdd.sh ${D}${sysconfdir}/init.d/
        install -m 0755 ${S}/crdd ${D}${sbindir}
}
