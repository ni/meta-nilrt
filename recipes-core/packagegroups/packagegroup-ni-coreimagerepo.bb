SUMMARY = "Packagegroup that contains all of the components required for building the OE feeds"
LICENSE = "MIT"
PR = "r1"

inherit packagegroup

RDEPENDS_${PN} = "\
	packagegroup-ni-base \
	packagegroup-ni-tzdata \
	packagegroup-ni-wifi \
	packagegroup-ni-runmode \
	packagegroup-ni-xfce \
	packagegroup-ni-crio \
	packagegroup-ni-restoremode \
	packagegroup-core-x11 \
	opkg-collateral \
	apache2 \
"
