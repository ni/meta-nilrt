SUMMARY = "Graphical packages for the NI Linux Realtime distribution"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} = "\
	packagegroup-ni-xfce \
	sysconfig-settings-ui \
"
