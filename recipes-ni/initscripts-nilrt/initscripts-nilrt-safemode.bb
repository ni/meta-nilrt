SUMMARY = "SysV nilrt init scripts for safemode"
DESCRIPTION = "nilrt distro-specific initscripts to provide basic system functionality."
HOMEPAGE = "https://github.com/ni/meta-nilrt"
SECTION = "base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS = "\
	niacctbase\
	pseudo-native \
	shadow-native \
	update-rc.d-native \
"

PV = "2.0"


SRC_URI = " \
"

SRC_URI:append:x64 = " \
	file://mountcompatibility \
	file://mountuserfs \
	file://nisafemodereason \
	file://niselectnetnaming \
"

S = "${WORKDIR}"


do_install () {
	install -d ${D}${sysconfdir}/init.d/
}

do_install:append:x64 () {
	install -m 0755   ${WORKDIR}/mountcompatibility     ${D}${sysconfdir}/init.d
	install -m 0755   ${WORKDIR}/mountuserfs            ${D}${sysconfdir}/init.d
	install -m 0755   ${WORKDIR}/nisafemodereason       ${D}${sysconfdir}/init.d
	install -m 0755   ${WORKDIR}/niselectnetnaming      ${D}${sysconfdir}/init.d

	update-rc.d -r ${D} mountcompatibility start 97 S .
	update-rc.d -r ${D} mountuserfs start 82 S .
	update-rc.d -r ${D} nisafemodereason start 60 S .
	update-rc.d -r ${D} niselectnetnaming start 3 S .
}


RDEPENDS:${PN} += "bash niacctbase update-rc.d"
