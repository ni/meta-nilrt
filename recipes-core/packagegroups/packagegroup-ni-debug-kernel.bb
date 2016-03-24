SUMMARY = "Debug kernel packages for the NI Linux Real-Time distribution"
LICENSE = "MIT"

inherit packagegroup

RDEPENDS_${PN} = "\
	linux-nilrt-debug \
	"
