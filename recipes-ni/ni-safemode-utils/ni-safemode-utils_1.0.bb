SUMMARY = "nilrt safemode utilities"
DESCRIPTION = "nilrt distro-specific safemode utilities that provide basic system functionality."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = "\
	file://nisafemodeversion \
	file://niinstallsafemode \
	file://nicompareversion \
"

natinstbin="/usr/local/natinst/bin"

FILES_${PN} += "\
	${natinstbin}/nisafemodeversion \
	${natinstbin}/niinstallsafemode \
	${natinstbin}/nicompareversion \
"

DEPENDS += "shadow-native pseudo-native niacctbase update-rc.d-native"

RDEPENDS_${PN} += "niacctbase bash"

RDEPENDS_${PN}_append_x64 += "fw-printenv"

S = "${WORKDIR}"

do_install () {
	install -d ${D}/usr/local/natinst/bin
	install -m 0755   ${WORKDIR}/nisafemodeversion           ${D}${natinstbin}
	install -m 0550   ${WORKDIR}/niinstallsafemode           ${D}${natinstbin}
	install -m 0755   ${WORKDIR}/nicompareversion            ${D}${natinstbin}

	chown 0:${LVRT_GROUP} ${D}${natinstbin}/niinstallsafemode
}
