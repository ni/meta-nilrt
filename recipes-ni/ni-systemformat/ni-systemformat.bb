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

inherit update-rc.d

INITSCRIPT_NAME = "nitargetinfo"
INITSCRIPT_PARAMS = "start 20 S ."


# ==============================================================================
# TASKS
# ==============================================================================

do_install () {
	oe_runmake install DESTDIR=${D}

	# Install project initscripts
	install -d ${D}${sysconfdir}/init.d
	install -m 0544 ${S}/init/nitargetinfo ${D}${sysconfdir}/init.d/nitargetinfo

	# The nisystemformat binary can be executed by anyone in the 'ni' group.
	chown 0:${LVRT_GROUP} ${D}${bindir}/nisystemformat

	# legacy symlink location
	install -d ${D}/usr/local/natinst/bin
	ln -sf ${bindir}/nisystemformat ${D}/usr/local/natinst/bin/nisystemformat
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
	pkgconfig \
	ni-netcfgutil \
	niacctbase \
	shadow \
	util-linux-lsblk \
	util-linux-logger \
"
# LUKS encryption support
RDEPENDS:${PN} += "\
	coreutils-shred \
	cryptsetup \
	ni-device-encryption \
"
