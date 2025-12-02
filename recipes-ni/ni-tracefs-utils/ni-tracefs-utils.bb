SUMMARY = "NI tracefs utility scripts"
DESCRIPTION = "Installs scripts for accessing the kernel tracing mechanism."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

PV = "1.0"

SRC_URI = "\
	file://traceconfig \
	file://traceextract \
	file://sudoers \
"

FILES:${PN} += "\
        ${datadir}/ni-tracefs-utils/* \
        ${sysconfdir}/sudoers.d/* \
"
CONFFILES:${PN} = "${sysconfdir}/sudoers.d/*"

RDEPENDS:${PN} += "niacctbase bash sudo-lib"

S = "${UNPACKDIR}"

do_install () {
	install -d ${D}${base_sbindir}

	install -m 0550   ${S}/traceconfig         ${D}${base_sbindir}
	install -m 0550   ${S}/traceextract         ${D}${base_sbindir}

        install -d ${D}${sysconfdir}/sudoers.d/
        install --mode=0660 ${S}/sudoers ${D}${sysconfdir}/sudoers.d/${PN}
}
