SUMMARY = "NI account sync setup"
DESCRIPTION = "Installs init.d setup scripts that synchronize account information"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

DEPENDS += "update-rc.d-native"

SRC_URI = "\
	file://ni-acctsync \
"

S = "${WORKDIR}"

do_install () {
	install -d ${D}${sysconfdir}/init.d/
	install -Dm 0755 ${WORKDIR}/ni-acctsync ${D}${sysconfdir}/init.d/
	update-rc.d -r ${D} ni-acctsync start 36 S . stop 11 0 6 .
}

RDEPENDS:${PN} += "\
	update-rc.d \
"
