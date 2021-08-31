SUMMARY = "A system imaging utility for NI LinuxRT safemodes"
DESCRIPTION = "Installs the nisystemimage utility"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

DEPENDS += "niacctbase"

SRC_URI = "\
	file://nisystemimage \
"

S = "${WORKDIR}"

do_install () {
	install -d ${D}${bindir}
	install -m 0550 ${S}/nisystemimage ${D}${bindir}
	chown 0:${LVRT_GROUP} ${D}${bindir}/nisystemimage

	# legacy symlink location
	install -d ${D}/usr/local/natinst/bin
	ln -sf ${bindir}/nisystemimage ${D}/usr/local/natinst/bin/nisystemimage
}


FILES_${PN} += "\
	${bindir}/nisystemimage \
	/usr/local/natinst/bin/nisystemimage \
"

RDEPENDS_${PN} += "bash ni-netcfgutil"
