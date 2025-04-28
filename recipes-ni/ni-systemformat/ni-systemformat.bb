SUMMARY = "A system formatting utility for NI LinuxRT"
DESCRIPTION = "\
Installs the nisystemformat utility; a disk configuration and formatting \
utility for use on NI devices and NI LinuxRT.\
"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

DEPENDS += "niacctbase"

SRC_URI = "\
	file://nisystemformat \
	file://nitargetinfo \
	file://nitargetinfo.service \
"

S = "${WORKDIR}"

script_location = "${@bb.utils.contains('INIT_MANAGER','sysvinit','${sysconfdir}/init.d','${systemd_unitdir}/scripts',d)}"
FILES:${PN} += "\
	${bindir}/nisystemformat \
	${script_location}/nitargetinfo \
	/usr/local/natinst/bin/nisystemformat \
	${@bb.utils.contains('INIT_MANAGER','systemd','${systemd_system_unitdir}/nitargetinfo.service','',d)} \
"

do_install () {
	install -d ${D}${bindir}
	install -m 0550 ${S}/nisystemformat ${D}${bindir}
	chown 0:${LVRT_GROUP} ${D}${bindir}/nisystemformat

	install -d ${D}${script_location}
	install -m 0755 ${S}/nitargetinfo ${D}${script_location}

	# legacy symlink location
	install -d ${D}/usr/local/natinst/bin
	ln -sf ${bindir}/nisystemformat ${D}/usr/local/natinst/bin/nisystemformat

	if ${@bb.utils.contains('INIT_MANAGER','systemd','true','false',d)}; then
		install -d ${D}${systemd_system_unitdir}
		install -m 0644 ${WORKDIR}/nitargetinfo.service ${D}${systemd_system_unitdir}
	fi
}

inherit update-rc.d systemd

INITSCRIPT_NAME = "nitargetinfo"
INITSCRIPT_PARAMS = "start 20 S ."
SYSTEMD_SERVICE:${PN} = "nitargetinfo.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

RDEPENDS:${PN} += "\
	bash \
	niacctbase \
"

# nisystemformat rdeps
RDEPENDS:${PN} += "\
	coreutils \
	e2fsprogs-mke2fs \
	ni-netcfgutil \
	util-linux-lsblk \
"
