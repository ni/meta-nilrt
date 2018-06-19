DESCRIPTION = "Customized settings to use the right fonts in XFCE."
SECTION = "x11"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
RDEPENDS_${PN} = "fontconfig"
S = "${WORKDIR}"

SRC_URI = "file://48-nilrt-override.conf"

FILES_${PN} = "${sysconfdir}/fonts"

do_install () {
	install -d ${D}/${sysconfdir}/fonts/conf.d
	install -d ${D}/${sysconfdir}/fonts/conf.avail
	install -m 0644 48-nilrt-override.conf ${D}/${sysconfdir}/fonts/conf.avail/48-nilrt-override.conf
	ln -sf ${sysconfdir}/fonts/conf.avail/48-nilrt-override.conf ${D}/${sysconfdir}/fonts/conf.d/48-nilrt-override.conf
}

