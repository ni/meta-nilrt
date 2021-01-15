SUMMARY = "NI SDboot Config"
DESCRIPTION = "Configuration script for arm target booting from SD Card"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

inherit update-rc.d

SRC_URI = "file://nisdbootconfig \
"

INITSCRIPT_NAME = "nisdbootconfig"
INITSCRIPT_PARAMS = "start 00 S ."

S = "${WORKDIR}"

do_install () {
	install -d ${D}${sysconfdir}/init.d/

	# from artemis onwards, using sd card as main disk for arm target is introduced
	# install nisdbootconfig to configure sd card booting requirement
	install -m 0755 ${WORKDIR}/nisdbootconfig ${D}${sysconfdir}/init.d
}
