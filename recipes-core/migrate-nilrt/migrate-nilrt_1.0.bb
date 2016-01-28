SUMMARY = "Scripts for migrating NI Targets from LV2015 to LVNext and back"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "file://ni_migrate_target "

RDEPENDS_${PN} += " bash "

PR = "r1"

S = "${WORKDIR}"

do_install() {
        install -d ${D}${base_sbindir}/
        install -m 0755 ${S}/ni_migrate_target ${D}${base_sbindir}
}

FILES_${PN} += "${base_sbindir}/ni_migrate_target "

