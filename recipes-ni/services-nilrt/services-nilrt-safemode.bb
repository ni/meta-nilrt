SUMMARY = "SystemD nilrt services for safemode"
DESCRIPTION = "nilrt distro-specific services to provide basic system functionality."
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

DEPENDS += "shadow-native pseudo-native niacctbase"

RDEPENDS:${PN} += "bash niacctbase"

SRC_URI = " \
	file://mountcompatibility.service \
	file://mountcompatibility \
	file://mountuserfs.service \
	file://mountuserfs \
	file://nisafemodereason.service \
	file://nisafemodereason \
	file://niselectnetnaming.service \
	file://niselectnetnaming \
"

inherit features_check systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"
SYSTEMD_SERVICE:${PN} = " \
	mountcompatibility.service \
	mountuserfs.service \
	nisafemodereason.service \
	niselectnetnaming.service \
"

FILES:${PN} += " \
	${systemd_unitdir}/system/mountcompatibility.service \
	${libdir}/systemd/scripts/mountcompatibility \
	${sysconfdir}/init.d/mountcompatibility \
	${systemd_unitdir}/system/mountuserfs.service \
	${libdir}/systemd/scripts/mountuserfs \
	${sysconfdir}/init.d/mountuserfs \
	${systemd_unitdir}/system/nisafemodereason.service \
	${libdir}/systemd/scripts/nisafemodereason \
	${systemd_unitdir}/system/niselectnetnaming.service \
	${libdir}/systemd/scripts/niselectnetnaming \
"

S = "${UNPACKDIR}"

do_install () {
	install -d ${D}${systemd_unitdir}/system
	install -d ${D}${libdir}/systemd/scripts

	install -m 0644 ${S}/mountcompatibility.service ${D}${systemd_unitdir}/system
	install -m 0755 ${S}/mountcompatibility ${D}${libdir}/systemd/scripts
	install -m 0644 ${S}/mountuserfs.service ${D}${systemd_unitdir}/system
	install -m 0755 ${S}/mountuserfs ${D}${libdir}/systemd/scripts
	install -m 0644 ${S}/nisafemodereason.service ${D}${systemd_unitdir}/system
	install -m 0755 ${S}/nisafemodereason ${D}${libdir}/systemd/scripts
	install -m 0644 ${S}/niselectnetnaming.service ${D}${systemd_unitdir}/system
	install -m 0755 ${S}/niselectnetnaming ${D}${libdir}/systemd/scripts
}

# Restore direct-call entry points after systemd.bbclass removes init.d.
do_install[postfuncs] += "install_compat_links"

install_compat_links () {
	install -d ${D}${sysconfdir}/init.d
	for script in mountcompatibility mountuserfs; do
		ln -rs ${D}${libdir}/systemd/scripts/$script ${D}${sysconfdir}/init.d/$script
	done
}

REQUIRED_DISTRO_FEATURES = " systemd"

RDEPENDS:${PN} += "\
	bash \
	niacctbase \
"
