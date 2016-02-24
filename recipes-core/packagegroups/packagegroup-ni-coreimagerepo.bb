SUMMARY = "Packagegroup that contains all of the components required for building the OE feeds"
LICENSE = "MIT"
PR = "r1"

inherit packagegroup

RDEPENDS_${PN} = "\
	packagegroup-ni-base \
	packagegroup-ni-tzdata \
	packagegroup-ni-wifi \
	packagegroup-ni-runmode \
	packagegroup-ni-crio \
	packagegroup-ni-restoremode \
	packagegroup-core-x11 \
	packagegroup-core-standalone-sdk-target \
	packagegroup-kernel-module-build \
	linux-firmware \
	apache2 \
	apr-iconv \
"

RDEPENDS_${PN}_append_x64 = "\
	packagegroup-ni-xfce \
"
