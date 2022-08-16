SUMMARY = "rtfeatures user-space tools"
DESCRIPTION = "Provides user-space tools to support the nirtfeatures kernel module."
HOMEPAGE = "https://github.com/ni/meta-nilrt"
SECTION = "base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS += "update-rc.d-native"

PV = "2.0"


SRC_URI += "\
	file://init.d/handle_cpld_ip_reset \
	file://rtfeatures.rules \
"

S = "${WORKDIR}"


inherit update-rc.d
INITSCRIPT_NAME = "handle_cpld_ip_reset"
INITSCRIPT_PARAMS = "start 6 1 3 4 5 ."


do_install:append () {
	install -d ${D}${sysconfdir}/init.d/
	install -m 0755 ${S}/init.d/handle_cpld_ip_reset    ${D}${sysconfdir}/init.d

	install -d ${D}${sysconfdir}/udev/rules.d
	install -m 0644 ${S}/rtfeatures.rules    ${D}${sysconfdir}/udev/rules.d/rtfeatures.rules
}


PACKAGE_ARCH = "all"
PACKAGES:remove += "${PN}-staticdev ${PN}-dev ${PN}-dbg"

FILES:${PN} += "\
	${sysconfdir}/init.d/handle_cpld_ip_reset \
	${sysconfdir}/udev \
"

RDEPENDS:${PN} += "\
	bash \
	ni-netcfgutil \
	udev \
"
