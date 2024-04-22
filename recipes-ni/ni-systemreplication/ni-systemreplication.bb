SUMMARY = "A system replication utility for NI LinuxRT"
DESCRIPTION = "Installs the nisystemreplication utility"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = "\
    file://nisystemreplication \
"

S = "${WORKDIR}"

do_install () {
    install -d ${D}${base_sbindir}
    install -m 0550 ${S}/nisystemreplication ${D}${base_sbindir}
}


FILES:${PN} += "\
    ${base_sbindir}/nisystemreplication \
"
RDEPENDS:${PN} += "\
    bash \
    ni-systemimage \
"
