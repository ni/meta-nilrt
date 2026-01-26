SUMMARY = "NI account sync setup"
DESCRIPTION = "Installs init.d setup scripts that synchronize account information"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

SRC_URI = "\
	file://ni-acctsync \
"

S = "${UNPACKDIR}"

inherit update-rc.d

INITSCRIPT_NAME = "ni-acctsync"
INITSCRIPT_PARAMS = "start 36 S . stop 11 0 6 ."

do_install () {
	install -d ${D}${sysconfdir}/init.d/
	install -m 0700 ${S}/ni-acctsync ${D}${sysconfdir}/init.d/
}
