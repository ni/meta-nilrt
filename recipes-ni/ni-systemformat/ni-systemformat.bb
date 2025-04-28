SUMMARY = "A system formatting utility for NI LinuxRT"
DESCRIPTION = "\
Installs the nisystemformat utility; a disk configuration and formatting \
utility for use on NI devices and NI LinuxRT.\
"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

DEPENDS += "niacctbase"

PV = "2.0.0"


# ==============================================================================
# SOURCES
# ==============================================================================

SRC_URI = "\
	file://src/nisystemformat \
	file://init/ \
	file://src/ \
	file://Makefile \
"

S = "${UNPACKDIR}"


# ==============================================================================
# BBCLASSES
# ==============================================================================

inherit update-rc.d systemd

INITSCRIPT_NAME = "nitargetinfo"
INITSCRIPT_PARAMS = "start 20 S ."
SYSTEMD_SERVICE:${PN} = "nitargetinfo.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

# ==============================================================================
# TASKS
# ==============================================================================

script_location = "${@bb.utils.contains('INIT_MANAGER','sysvinit','${sysconfdir}/init.d','${systemd_unitdir}/scripts',d)}"
FILES:${PN} += "\
	${bindir}/nisystemformat \
	${script_location}/nitargetinfo \
	/usr/local/natinst/bin/nisystemformat \
	${@bb.utils.contains('INIT_MANAGER','systemd','${systemd_system_unitdir}/nitargetinfo.service','',d)} \
"

do_install () {
	oe_runmake install DESTDIR=${D}

	# Install project initscripts
	install -d ${D}${script_location}
	install -m 0755 ${S}/init/nitargetinfo ${D}${script_location}/nitargetinfo

	# The nisystemformat binary can be executed by anyone in the 'ni' group.
	chown 0:${LVRT_GROUP} ${D}${bindir}/nisystemformat

	# legacy symlink location
	install -d ${D}/usr/local/natinst/bin
	ln -sf ${bindir}/nisystemformat ${D}/usr/local/natinst/bin/nisystemformat

	if ${@bb.utils.contains('INIT_MANAGER','systemd','true','false',d)}; then
		install -d ${D}${systemd_system_unitdir}
		install -m 0644 ${S}/init/nitargetinfo.service ${D}${systemd_system_unitdir}/
	fi
}

# ==============================================================================
# PACKAGING
# ==============================================================================

FILES:${PN} += "\
	/usr/local/natinst/bin/nisystemformat \
"

RDEPENDS:${PN} += "\
	bash \
	coreutils \
	e2fsprogs-mke2fs \
	ni-netcfgutil \
	niacctbase \
	${@bb.utils.contains('INIT_MANAGER', 'systemd', 'ni-shutdown-guard', '', d)} \
	shadow \
	util-linux-blkid \
	util-linux-lsblk \
	util-linux-logger \
"
# LUKS encryption support
RDEPENDS:${PN}:append:x64 = "\
	coreutils-shred \
	cryptsetup \
	ni-device-encryption \
"
