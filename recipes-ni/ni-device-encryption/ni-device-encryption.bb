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
	file://src/ \
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
# BUILD DEPENDENCIES
# ==============================================================================

# openssl: required to build ni-pcr-precalc (links against -lcrypto)
DEPENDS += "openssl"


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
	ln -sf ${libdir}/${BPN}/share ${D}${datadir}/${BPN}

	# Install sbin symlinks
	install -d ${D}${sbindir}
	ln -sf ${libdir}/${BPN}/bin/ni-reseal-luks.sh ${D}${sbindir}/ni-reseal-luks
	ln -sf ${libdir}/${BPN}/bin/ni-pcr-precalc ${D}${sbindir}/ni-pcr-precalc

	# Install PCR artifact lists to the package share directory, where they
	# are reachable from both the safemode initramfs and the runmode rootfs.
	install -m 0644 ${S}/share/ni-pcr-runmode-artifacts.list ${D}${libdir}/${BPN}/share/ni-pcr-runmode-artifacts.list
	install -m 0644 ${S}/share/ni-pcr-safemode-artifacts.list ${D}${libdir}/${BPN}/share/ni-pcr-safemode-artifacts.list

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
	lvm2 \
	util-linux-blkid \
	util-linux-logger \
"

FILES:${PN} += "${libdir}/${BPN}/share/ni-pcr-runmode-artifacts.list ${libdir}/${BPN}/share/ni-pcr-safemode-artifacts.list"
