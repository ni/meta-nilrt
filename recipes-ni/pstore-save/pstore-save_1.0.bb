SUMMARY = "pstore state recovery utility"
DESCRIPTION = "Restores saved pstore state after system crashes"
SECTION = "base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit update-rc.d

S = "${WORKDIR}"

SRC_URI = "\
    file://pstore-save \
    file://initscript \
    file://run-ptest \
    file://testdata/ \
"

INITSCRIPT_NAME = "pstore-save"
INITSCRIPT_PARAMS = "start 09 S ."

DEPENDS += "bash"
RDEPENDS_${PN} += "bash"

do_install () {
    install -d ${D}${sbindir} ${D}${sysconfdir}/init.d
    install -m 0755 ${S}/pstore-save ${D}${sbindir}/

    install -m 0755 ${S}/initscript ${D}${sysconfdir}/init.d/pstore-save
}

inherit ptest

RDEPENDS_${PN}-ptest += "${PN} bash"

do_install_ptest_append () {
    install -d ${D}${PTEST_PATH}/src
    install -m 0444 ${S}/testdata/src/* ${D}${PTEST_PATH}/src

    install -d ${D}${PTEST_PATH}/expected
    install -m 0444 ${S}/testdata/expected/* ${D}${PTEST_PATH}/expected
}
