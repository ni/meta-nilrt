SUMMARY = "nilrt safemode utilities"
DESCRIPTION = "nilrt distro-specific safemode utilities that provide basic system functionality."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"
S = "${UNPACKDIR}"

SRC_URI = "\
	file://nicompareversion  \
	file://niinstallsafemode \
	file://nisafemodeversion \
"

natinstbin = "/usr/local/natinst/bin"

FILES:${PN} += "\
	${natinstbin}/nicompareversion   \
	${natinstbin}/niinstallsafemode  \
	${natinstbin}/nisafemodeversion  \
"

DEPENDS += "shadow-native pseudo-native niacctbase update-rc.d-native"

RDEPENDS:${PN} += "niacctbase bash fw-printenv"
RDEPENDS:${PN}:append:xilinx-zynq = " u-boot-tools-mkimage "

do_install () {
	install -d ${D}${natinstbin}

	install -m 0755   ${UNPACKDIR}/nicompareversion            ${D}${natinstbin}
	install -m 0550   ${UNPACKDIR}/niinstallsafemode           ${D}${natinstbin}
	install -m 0755   ${UNPACKDIR}/nisafemodeversion           ${D}${natinstbin}

	chown 0:${LVRT_GROUP} ${D}${natinstbin}/niinstallsafemode
}
