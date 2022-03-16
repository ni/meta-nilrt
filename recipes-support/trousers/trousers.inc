SUMMARY = "TrouSerS - An open-source TCG Software Stack implementation, created and released by IBM."
DESCRIPTION = " \
  Trousers is an open-source TCG Software Stack (TSS), released under the \
  Common Public License. Trousers aims to be compliant with the current (1.1b) \
  and upcoming (1.2) TSS specifications available from the Trusted Computing \
  Group website: http://www.trustedcomputinggroup.org. \
  "
SECTION = "tpm"
PR = "r0"
LICENSE = "CPL-1.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=8031b2ae48ededc9b982c08620573426"
DEPENDS = "openssl"

SRC_URI += "http://sourceforge.net/projects/trousers/files/trousers/${PV}/trousers-${PV}.tar.gz \
            file://trousers.init.sh \
            file://trousers-udev.rules \
            file://tcsd.service \
"

PROVIDES = "${PACKAGES}"
PACKAGES = " \
	libtspi \
	libtspi-dbg \
	libtspi-dev \
	libtspi-doc \
	libtspi-staticdev \
	trousers \
	trousers-dbg \
	trousers-doc \
	"

FILES_libtspi = " \
	${libdir}/*.so.1.2.0 \
	${libdir}/*.so.1 \
	"
FILES_libtspi-dbg = " \
	${libdir}/.debug \
	${prefix}/src/debug/${PN}/${PV}-${PR}/${PN}-${PV}/src/tspi \
	${prefix}/src/debug/${PN}/${PV}-${PR}/${PN}-${PV}/src/trspi \
	${prefix}/src/debug/${PN}/${PV}-${PR}/${PN}-${PV}/src/include/*.h \
	${prefix}/src/debug/${PN}/${PV}-${PR}/${PN}-${PV}/src/include/tss \
	"
FILES_libtspi-dev = " \
	${includedir} \
	${libdir}/*.so \
	"
FILES_libtspi-doc = " \
	${mandir}/man3 \
	"
FILES_libtspi-staticdev = " \
	${libdir}/*.la \
	${libdir}/*.a \
	"
FILES_${PN} = " \
	${sbindir}/tcsd \
	${sysconfdir} \
	${localstatedir} \
	"
FILES_${PN}-dbg += " \
	${sbindir}/.debug \
	${prefix}/src/debug \
	"
FILES_${PN}-doc += " \
	${mandir}/man5 \
	${mandir}/man8 \
	"

inherit autotools pkgconfig useradd update-rc.d systemd

INITSCRIPT_NAME = "trousers"
INITSCRIPT_PARAMS = "start 99 2 3 4 5 . stop 19 0 1 6 ."

EXTRA_OECONF="--with-gui=none"

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM_${PN} = "--system tss"
USERADD_PARAM_${PN} = "--system -M -d /var/lib/tpm -s /bin/false -g tss tss"

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = "tcsd.service"
SYSTEMD_AUTO_ENABLE = "disable"

do_install_append() {
        install -d ${D}${sysconfdir}/init.d
        install -m 0755 ${WORKDIR}/trousers.init.sh ${D}${sysconfdir}/init.d/trousers
        install -d ${D}${sysconfdir}/udev/rules.d
        install -m 0644 ${WORKDIR}/trousers-udev.rules ${D}${sysconfdir}/udev/rules.d/45-trousers.rules
        install -d ${D}${systemd_unitdir}/system
        install -m 0644 ${WORKDIR}/tcsd.service ${D}${systemd_unitdir}/system/
        sed -i -e 's#@SBINDIR@#${sbindir}#g' ${D}${systemd_unitdir}/system/tcsd.service
}

BBCLASSEXTEND = "native"
