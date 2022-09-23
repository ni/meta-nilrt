SUMMARY = "Debug kernel packages for the NI Linux Real-Time distribution"
LICENSE = "MIT"

inherit packagegroup

RDEPENDS:${PN} = "\
	kernel-debug \
	kernel-debug-vmlinux \
	kernel-debug-dev \
	kernel-debug-modules \
	kernel-devsrc-debug \
"

RDEPENDS:${PN}:append:armv7a = " \
	kernel-debug-devicetree \
"
