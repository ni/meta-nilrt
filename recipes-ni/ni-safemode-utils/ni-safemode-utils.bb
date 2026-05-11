SUMMARY = "nilrt safemode utilities"
DESCRIPTION = "nilrt distro-specific safemode utilities that provide basic system functionality."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

NIINSTALLSAFEMODE:x64 = "niinstallsafemode.x64"
NIINSTALLSAFEMODE:xilinx-zynq = "niinstallsafemode.arm"

SRC_URI = "\
	file://nicompareversion  \
	file://${NIINSTALLSAFEMODE} \
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

	install -m 0755   ${WORKDIR}/nicompareversion            ${D}${natinstbin}
	install -m 0550   ${WORKDIR}/${NIINSTALLSAFEMODE}        ${D}${natinstbin}/niinstallsafemode
	install -m 0755   ${WORKDIR}/nisafemodeversion           ${D}${natinstbin}

	chown 0:${LVRT_GROUP} ${D}${natinstbin}/niinstallsafemode
}
