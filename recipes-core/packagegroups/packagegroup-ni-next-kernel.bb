SUMMARY = "Kernel packages for 'next' kernel under test for the NI Linux Real-Time distribution"
LICENSE = "MIT"

inherit packagegroup

RDEPENDS_${PN} = "\
	kernel-next \
	kernel-next-dev \
	kernel-next-modules \
"
