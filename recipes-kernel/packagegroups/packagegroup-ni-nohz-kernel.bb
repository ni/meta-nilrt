SUMMARY = "Kernel packages for a full dynamic ticks 'nohz' kernel for the NI Linux Real-Time distribution"
LICENSE = "MIT"

inherit packagegroup

RDEPENDS_${PN} = "\
	kernel-nohz \
	kernel-nohz-dev \
	kernel-nohz-modules \
	kernel-devsrc-nohz \
"
