SUMMARY = "rtfeatures user-space tools"
DESCRIPTION = "Provides user-space tools to support the nirtfeatures kernel module."
HOMEPAGE = "https://github.com/ni/meta-nilrt"
SECTION = "base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"


DEPENDS += "\
	${@bb.utils.contains('INIT_MANAGER','sysvinit','update-rc.d-native','systemd-systemctl-native',d)} \
"

PV = "2.1"

SRC_URI += "\
	file://handle_cpld_ip_reset.initd \
	file://ni-rtfeatures.initd \
	file://rtfeatures.rules \
	file://handle_cpld_ip_reset.service \
	file://ni-rtfeatures.service \
"

S = "${WORKDIR}"

inherit allarch systemd
PACKAGE_ARCH = "all"
PACKAGES:remove = "${PN}-staticdev ${PN}-dev ${PN}-dbg"

SYSTEMD_SERVICE:${PN} = " \
	handle_cpld_ip_reset.service \
	ni-rtfeatures.service \
"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

script_location = "${@bb.utils.contains('INIT_MANAGER','sysvinit','${sysconfdir}/init.d','${systemd_unitdir}/scripts',d)}"
FILES:${PN} += "\
	${script_location}/handle_cpld_ip_reset \
	${script_location}/ni-rtfeatures \
	${sysconfdir}/udev \
	${@bb.utils.contains('INIT_MANAGER','systemd','${systemd_system_unitdir}/handle_cpld_ip_reset.service','',d)} \
	${@bb.utils.contains('INIT_MANAGER','systemd','${systemd_system_unitdir}/ni-rtfeatures.service','',d)} \
"

do_install:append () {
	install -d ${D}${script_location}/
	install -m 0755 ${S}/handle_cpld_ip_reset.initd  ${D}${script_location}/handle_cpld_ip_reset
	install -m 0755 ${S}/ni-rtfeatures.initd         ${D}${script_location}/ni-rtfeatures

	install -d ${D}${sysconfdir}/udev/rules.d
	install -m 0644 ${S}/rtfeatures.rules    ${D}${sysconfdir}/udev/rules.d/rtfeatures.rules

	if ${@bb.utils.contains('INIT_MANAGER','systemd','true','false',d)}; then
		install -d ${D}${systemd_system_unitdir}
		install -m 0644 ${WORKDIR}/handle_cpld_ip_reset.service ${D}${systemd_system_unitdir}/
		install -m 0644 ${WORKDIR}/ni-rtfeatures.service ${D}${systemd_system_unitdir}/
	fi
}

python __anonymous() {
    if bb.utils.contains('INIT_MANAGER','sysvinit',True,False,d):
        d.appendVar('pkg_postinst:${PN}',"""
if [ -n "$D" ]; then
    OPT="-r $D"
else
    OPT=""
fi
update-rc.d $OPT handle_cpld_ip_reset start 6 1 3 4 5 .
update-rc.d $OPT ni-rtfeatures start 20 1 3 4 5 .
"""
)
        d.appendVar('pkg_postrm:${PN}',"""
if [ -n "$D" ]; then
    OPT="-f -r $D"
else
    OPT="-f"
fi
update-rc.d $OPT handle_cpld_ip_reset remove
update-rc.d $OPT ni-rtfeatures remove
"""
)
}

RDEPENDS:${PN} += "\
	bash \
	ni-netcfgutil \
	udev \
	util-linux \
"
