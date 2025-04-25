SUMMARY = "Support scripts and utilities common to all NI hardware products"
DESCRIPTION = "Support scripts and utilities for all NI hardware products which are supported by NI LinuxRT."
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

DEPENDS += "\
	${@bb.utils.contains('INIT_MANAGER','sysvinit','update-rc.d-native','systemd-systemctl-native',d)} \
"

SRC_URI += "\
	file://init.d/ni-rename-ifaces \
	file://init.d/nisetserialnumber \
	file://ni-rename-ifaces.service \
	file://nisetserialnumber.service \
"

S = "${WORKDIR}"

inherit allarch systemd
PACKAGE_ARCH = "all"
PACKAGES:remove = "${PN}-staticdev ${PN}-dev ${PN}-dbg"


do_install () {
	install -d ${D}${script_location}

	install -m 0755 ${S}/init.d/ni-rename-ifaces     ${D}${script_location}
	install -m 0755 ${S}/init.d/nisetserialnumber    ${D}${script_location}
}

python __anonymous() {
    if bb.utils.contains('INIT_MANAGER','sysvinit',True,False,d):
        d.appendVar('pkg_postinst:${PN}',"""
if [ -n "$D" ]; then
    OPT="-r $D"
else
    OPT="-s"
fi

update-rc.d $OPT ni-rename-ifaces    start 38 S .
update-rc.d $OPT nisetserialnumber   start 38 S .
"""
)
        d.appendVar('pkg_postrm:${PN}',"""
if [ -n "$D" ]; then
    OPT="-f -r $D"
else
    OPT="-f"
fi

update-rc.d $OPT ni-rename-ifaces  remove
update-rc.d $OPT nisetserialnumber remove
""")
}

script_location = "${@bb.utils.contains('INIT_MANAGER','sysvinit','${sysconfdir}/init.d','${systemd_unitdir}/scripts',d)}"
FILES:${PN} += "\
	${script_location}/ni-rename-ifaces \
	${script_location}/nisetserialnumber \
	${@bb.utils.contains('INIT_MANAGER','systemd','${systemd_system_unitdir}/ni-rename-ifaces.service','',d)} \
	${@bb.utils.contains('INIT_MANAGER','systemd','${systemd_system_unitdir}/nisetserialnumber.service','',d)} \
"

RDEPENDS:${PN} += "\
	bash \
"
