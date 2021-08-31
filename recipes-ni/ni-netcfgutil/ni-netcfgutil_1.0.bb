SUMMARY = "NI network configuration utility"
DESCRIPTION = "Installs the ninetcfgutil utility"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

DEPENDS += "niacctbase"

SRC_URI = "\
	file://ninetcfgutil \
	file://niresetip \
	file://${MACHINE}/ninetcfgutil_platdep.sh \
"

S = "${WORKDIR}"

do_install () {
	install -d ${D}${bindir}
	install -m 0550 ${S}/ninetcfgutil ${D}${bindir}
	chown 0:${LVRT_GROUP} ${D}${bindir}/ninetcfgutil
	install -m 0550 ${S}/niresetip ${D}${bindir}
	chown 0:${LVRT_GROUP} ${D}${bindir}/niresetip

	install -d ${D}/etc/natinst/networking
	install -m 0550 ${S}/${MACHINE}/ninetcfgutil_platdep.sh ${D}/etc/natinst/networking
	chown 0:${LVRT_GROUP} ${D}/etc/natinst/networking/ninetcfgutil_platdep.sh

	# legacy symlink location
	install -d ${D}/usr/local/natinst/bin
	ln -sf ${bindir}/ninetcfgutil ${D}/usr/local/natinst/bin/ninetcfgutil
	ln -sf ${bindir}/niresetip ${D}/usr/local/natinst/bin/niresetip
}


FILES_${PN} += "\
	${bindir}/ninetcfgutil \
	${bindir}/niresetip \
	/usr/local/natinst/bin/ninetcfgutil \
	/usr/local/natinst/bin/niresetip \
	/etc/natinst/networking/ninetcfgutil_platdep.sh \
"

RDEPENDS_${PN} += "bash"
