SUMMARY = "A system formatting utility for NI LinuxRT"
DESCRIPTION = "\
Installs the nisystemformat utility; a disk configuration and formatting \
utility for use on NI devices and NI LinuxRT.\
"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"


DEPENDS += "niacctbase"

SRC_URI = "\
	file://nisystemformat \
	file://nitargetinfo \
"

S = "${WORKDIR}"

do_install () {
	install -d ${D}${bindir}
	install -m 0550 ${S}/nisystemformat ${D}${bindir}
	chown 0:${LVRT_GROUP} ${D}${bindir}/nisystemformat

	install -d ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/nitargetinfo ${D}${sysconfdir}/init.d/

    # legacy symlink location
    install -d ${D}/usr/local/natinst/bin
    ln -sf ${bindir}/nisystemformat ${D}/usr/local/natinst/bin/nisystemformat
}


FILES_${PN} += "\
	${bindir}/nisystemformat \
	${sysconfdir}/init.d/nitargetinfo \
	/usr/local/natinst/bin/nisystemformat \
"

inherit update-rc.d

INITSCRIPT_NAME = "nitargetinfo"
INITSCRIPT_PARAMS = "start 20 S ."

RDEPENDS_${PN} += "bash"
