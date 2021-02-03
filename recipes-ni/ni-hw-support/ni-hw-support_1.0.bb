SUMMARY = "Support scripts and utilities for NI hardware products"
DESCRIPTION = "Support scripts and utilities for all NI hardware products which are supported by NI LinuxRT."
LICENSE = "MIT"
SECTION = "base"

inherit packagegroup

RDEPENDS_${PN} = "\
	${PN}-common \
"
