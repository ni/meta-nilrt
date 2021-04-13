SUMMARY = "Debug kernel packages for the NI Linux Real-Time distribution"
LICENSE = "MIT"

inherit packagegroup

RDEPENDS_${PN} = "\
	kernel-debug \
	kernel-debug-vmlinux \
	kernel-debug-dev \
	kernel-debug-modules \
	kernel-devsrc-debug \
"

RDEPENDS_${PN}_append_armv7a = " \
	kernel-debug-devicetree \
"
