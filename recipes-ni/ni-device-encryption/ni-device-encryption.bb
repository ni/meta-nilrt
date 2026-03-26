SUMMARY = "Tools for encrypting NI devices."
DESCRIPTION = "\
This package contains utilities for creating and using LUKS-encrypted root \
partitions for NILRT. Installs the ni-cryptdisks.sh initscript.\
"
HOMEPAGE = "https://github.com/ni/meta-nilrt"
SECTION = "security"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"


# ==============================================================================
# SOURCES
# ==============================================================================

SRC_URI = "\
	file://Makefile \
	file://bin/ \
	file://init/ \
	file://share/ \
"

S = "${UNPACKDIR}"


# ==============================================================================
# BBCLASSES
# ==============================================================================

# UPDATE-RC.D
inherit update-rc.d
INITSCRIPT_PARAMS = "start 30 S ."
INITSCRIPT_NAME = "ni-cryptdisks.sh"


# ==============================================================================
# TASKS
# ==============================================================================

EXTRA_OEMAKE += "DESTDIR=${D} VERSION=${PV}"


do_compile () {
	oe_runmake all
}


do_install () {
	oe_runmake install

	# Create a symlink to the package share directory from the prefix share, for compat w/ other packages
	install -d ${D}${datadir}
	ln -sf ../lib/${BPN}/share ${D}${datadir}/${BPN}

	# Install sbin symlink
	install -d ${D}${sbindir}
	ln -sf ../lib/${BPN}/bin/ni-reseal-luks.sh ${D}${sbindir}/ni-reseal-luks

	# Install initscript
	install -d ${D}${sysconfdir}/init.d
	install ${S}/init/ni-cryptdisks.sh ${D}${sysconfdir}/init.d/ni-cryptdisks.sh
}


# ==============================================================================
# PACKAGING
# ==============================================================================

# NI-DEVICE-ENCRYPTION
RDEPENDS:${PN} = "\
	bash \
	clevis \
	cryptsetup \
	libdevmapper \
	util-linux-blkid \
	util-linux-logger \
"
