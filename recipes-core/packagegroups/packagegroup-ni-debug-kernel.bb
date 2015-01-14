SUMMARY = "Debug kernel packages for the NI Linux Real-Time distribution"
LICENSE = "MIT"
PR = "r1"

inherit packagegroup

RDEPENDS_${PN} = "\
	linux-nilrt-debug \
	"
