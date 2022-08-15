SUMMARY = "nilrt safemode utilities"
DESCRIPTION = "nilrt distro-specific safemode utilities that provide basic system functionality."
HOMEPAGE = "https://github.com/ni/meta-nilrt"
SECTION = "base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS = "niacctbase"

PV = "2.0"


SRC_URI = "\
	file://nicompareversion  \
	file://niinstallsafemode \
	file://nisafemodeversion \
"
S = "${WORKDIR}"


natinstbin="/usr/local/natinst/bin"

do_install () {
	install -d ${D}${natinstbin}
	install -m 0550 ${S}/niinstallsafemode ${D}${natinstbin}
	install -m 0755 ${S}/nicompareversion  ${D}${natinstbin}
	install -m 0755 ${S}/nisafemodeversion ${D}${natinstbin}

	chown 0:${LVRT_GROUP} ${D}${natinstbin}/niinstallsafemode
}


FILES:${PN} += "${natinstbin}/*"

RDEPENDS:${PN} += "\
	bash \
	fw-printenv\
	niacctbase \
"
