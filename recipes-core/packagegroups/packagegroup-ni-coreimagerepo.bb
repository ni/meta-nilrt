SUMMARY = "Packagegroup that contains all of the components required for building the OE feeds"
LICENSE = "MIT"

inherit packagegroup

RDEPENDS_${PN} = "\
	packagegroup-ni-base \
	packagegroup-ni-ptest \
	packagegroup-ni-tzdata \
	packagegroup-ni-wifi \
	packagegroup-ni-runmode \
	packagegroup-ni-crio \
	packagegroup-ni-restoremode \
	${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'packagegroup-core-x11', '', d)} \
	packagegroup-core-standalone-sdk-target \
	packagegroup-kernel-module-build \
	apache2 \
	apache-websocket \
	apr-iconv \
	dkms \
"

RDEPENDS_${PN}_append_x64 = "\
	${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'packagegroup-ni-xfce', '', d)} \
	init-nilrt-ramfs \
"
