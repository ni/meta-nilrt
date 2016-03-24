DESCRIPTION = "Customized settings to use the right fonts in XFCE."
SECTION = "x11"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
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

