SUMMARY = "pstore state recovery utility"
DESCRIPTION = "Restores saved pstore state after system crashes"
SECTION = "base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS += "bash"

PV = "2.0"


SRC_URI = "\
	file://initscript \
	file://pstore-save \
	file://run-ptest \
	file://testdata/ \
"

S = "${WORKDIR}"


inherit update-rc.d
INITSCRIPT_NAME = "pstore-save"
INITSCRIPT_PARAMS = "start 09 S ."

inherit ptest
RDEPENDS:${PN}-ptest += "${PN} bash"


do_install () {
	install -d ${D}${sbindir}
	install -m 0755 ${S}/pstore-save ${D}${sbindir}/

	install -d ${D}${sbindir} ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/initscript ${D}${sysconfdir}/init.d/pstore-save
}

do_install_ptest:append () {
	install -d ${D}${PTEST_PATH}/src
	install -m 0444 ${S}/testdata/src/* ${D}${PTEST_PATH}/src

	install -d ${D}${PTEST_PATH}/expected
	install -m 0444 ${S}/testdata/expected/* ${D}${PTEST_PATH}/expected
}


RDEPENDS:${PN} += "bash"
