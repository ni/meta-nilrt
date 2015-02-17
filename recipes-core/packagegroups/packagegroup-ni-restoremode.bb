SUMMARY = "initramfs specific packages for NI Linux Realtime distribution"
LICENSE = "MIT"
PR = "r1"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} += "\
	init-restore-mode \
"
