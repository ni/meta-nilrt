SUMMARY = "pstore state recovery utility"
DESCRIPTION = "Restores saved pstore state after system crashes"
SECTION = "base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit update-rc.d systemd

S = "${WORKDIR}"

SRC_URI = "\
	file://pstore-save \
	file://initscript \
	file://run-ptest \
	file://testdata/ \
	file://pstore-save.service \
"

INITSCRIPT_NAME = "pstore-save"
INITSCRIPT_PARAMS = "start 09 S ."
SYSTEMD_SERVICE:${PN} = "pstore-save.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

DEPENDS += "bash"
RDEPENDS:${PN} += "bash"

script_location = "${@bb.utils.contains('INIT_MANAGER','sysvinit','${sysconfdir}/init.d','${systemd_unitdir}/scripts',d)}"
FILES:${PN} += "\
	${script_location}/pstore-save \
	${sbindir}/pstore-save \
	${@bb.utils.contains('INIT_MANAGER','systemd','${systemd_system_unitdir}/pstore-save.service','',d)} \
"
do_install () {
	install -d ${D}${sbindir} ${D}${script_location}
	install -m 0755 ${S}/pstore-save ${D}${sbindir}/

	install -m 0755 ${S}/initscript ${D}${script_location}/pstore-save
	if ${@bb.utils.contains('INIT_MANAGER','systemd','true','false',d)}; then
		install -d ${D}${systemd_system_unitdir}
		install -m 0644 ${WORKDIR}/pstore-save.service ${D}${systemd_system_unitdir}/
	fi
}

inherit ptest

RDEPENDS:${PN}-ptest += "${PN} bash"

do_install_ptest:append () {
	install -d ${D}${PTEST_PATH}/src
	install -m 0444 ${S}/testdata/src/* ${D}${PTEST_PATH}/src

	install -d ${D}${PTEST_PATH}/expected
	install -m 0444 ${S}/testdata/expected/* ${D}${PTEST_PATH}/expected
}
