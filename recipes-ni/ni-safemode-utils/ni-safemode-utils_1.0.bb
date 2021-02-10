SUMMARY = "nilrt safemode utilities"
DESCRIPTION = "nilrt distro-specific safemode utilities that provide basic system functionality."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = "\
	file://bootimage.ini     \
	file://nicompareversion  \
	file://niinstallsafemode \
	file://nisafemodeversion \
"

natinstbin="/usr/local/natinst/bin"

FILES_${PN} += "\
	/boot/bootimage.ini              \
	${natinstbin}/nicompareversion   \
	${natinstbin}/niinstallsafemode  \
	${natinstbin}/nisafemodeversion  \
"

DEPENDS += "shadow-native pseudo-native niacctbase update-rc.d-native"

RDEPENDS_${PN} += "niacctbase bash fw-printenv"

do_install () {
	install -d ${D}/boot
	install -d ${D}${natinstbin}

	install -m 0644   ${WORKDIR}/bootimage.ini               ${D}/boot
	install -m 0755   ${WORKDIR}/nicompareversion            ${D}${natinstbin}
	install -m 0550   ${WORKDIR}/niinstallsafemode           ${D}${natinstbin}
	install -m 0755   ${WORKDIR}/nisafemodeversion           ${D}${natinstbin}

	chown 0:${LVRT_GROUP} ${D}${natinstbin}/niinstallsafemode
}
