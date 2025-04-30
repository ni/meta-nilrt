SUMMARY = "rtctl - Utilities for controlling scheduling priorities of system threads"
SECTION = "System Environment/Daemons"
LICENSE = "GPL-2.0-or-later"

# No license file included in source. Hashing the "License" line in
# spec file.
LIC_FILES_CHKSUM = "file://${S}/rtctl.spec;beginline=6;endline=6;md5=cb929ed8d4eeb8a538033622343c6f94"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI = " \
    http://git.centos.org/sources/rtctl/c7-rt/eb046c6fb02eff54d1907c2b003bbc076eed6ded;downloadfilename=rtctl-${PV}.tar.bz2 \
    file://0001-rtctl-Read-task-IDs-from-procfs-instead-of-ps.patch \
    file://0002-rtctl-Support-multiple-rtgroups-files.patch \
    file://rtctl-${PV}/rtctld.c \
    file://rtctl-${PV}/init.d/rtctld \
    file://rtgroups \
    file://rtctl.service \
"
SRC_URI[sha256sum] = "33706ea797f99054049c20d709ca0e5c8ae5daccf347b80e8ac2884266439101"
SRC_URI[md5sum] = "a530ceb797193c54b0d57a05b1e82d24"

S = "${WORKDIR}/rtctl-${PV}"

RDEPENDS:${PN} += "bash"

inherit update-rc.d systemd

INITSCRIPT_NAME = "rtctld"
INITSCRIPT_PARAMS = "start 99 S . stop 00 0 . stop 00 6 ."
SYSTEMD_SERVICE:${PN} = "rtctl.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

script_location = "${@bb.utils.contains('INIT_MANAGER','sysvinit','${sysconfdir}/init.d','${systemd_unitdir}/scripts',d)}"
FILES:${PN} += "\
	${script_location}/rtctld \
	${sysconfdir}/rtgroups \
	${sbindir}/rtctl \
	${sbindir}/rtctld \
	${@bb.utils.contains('INIT_MANAGER','systemd','${systemd_system_unitdir}/rtctl.service','',d)} \
"

do_compile() {
    ${CC} -Os ${CFLAGS} rtctld.c -o rtctld ${LDFLAGS}
    cp "${WORKDIR}/rtgroups" "${S}/rtgroups"
}

do_install() {
    install -m 0755 -d ${D}${sbindir}/
    install -m 0755 -d ${D}${sysconfdir}/rtgroups.d/
    install -m 0755 -d ${D}${script_location}/

    install -m 0755 ${S}/rtctl ${D}${sbindir}/
    install -m 0755 ${S}/rtctld ${D}${sbindir}/

    install -m 0644 ${S}/rtgroups ${D}${sysconfdir}/

    install -m 0755 ${S}/init.d/rtctld ${D}${script_location}/

    if ${@bb.utils.contains('INIT_MANAGER','systemd','true','false',d)}; then
        install -d ${D}${systemd_system_unitdir}
        install -m 0644 ${WORKDIR}/rtctl.service ${D}${systemd_system_unitdir}/
    fi
}
