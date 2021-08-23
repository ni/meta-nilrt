SUMMARY = "Packagegroup that contains all of the components required for building the OE feeds"
LICENSE = "MIT"

inherit packagegroup

RDEPENDS_${PN} = "\
	packagegroup-base \
	packagegroup-core-boot \
	packagegroup-ni-base \
	packagegroup-ni-crio \
	packagegroup-ni-ptest \
	packagegroup-ni-restoremode \
	packagegroup-ni-runmode \
	packagegroup-ni-safemode \
	packagegroup-ni-transconf \
	packagegroup-ni-tzdata \
	packagegroup-ni-wifi \
	${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'packagegroup-core-x11', '', d)} \
	packagegroup-core-standalone-sdk-target \
	packagegroup-kernel-module-build \
	apache2 \
	apache-websocket \
	apr-iconv \
	dkms \
	ni-grpc-device \
"

RDEPENDS_${PN}_append_x64 = "\
	${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'packagegroup-ni-xfce', '', d)} \
	rauc \
	rauc-mark-good \
	init-nilrt-ramfs \
	nilrt-grub-safemode \
"
