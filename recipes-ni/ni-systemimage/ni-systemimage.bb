SUMMARY = "A system imaging utility for NI NILRT safemodes"
DESCRIPTION = "Installs the nisystemimage utility"
HOMEPAGE = "https://github.com/ni/meta-nilrt"
SECTION = "base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS += "niacctbase"

PV = "2.1"


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


FILES:${PN} += "\
	${bindir}/nisystemimage \
	/usr/local/natinst/bin/nisystemimage \
"
RDEPENDS:${PN} += "\
	bash \
	ni-netcfgutil \
	niacctbase \
"
