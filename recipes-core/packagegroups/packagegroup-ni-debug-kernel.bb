SUMMARY = "Debug kernel packages for the NI Linux Real-Time distribution"
LICENSE = "MIT"

inherit packagegroup

RDEPENDS_${PN} = "\
	kernel-debug \
	kernel-debug-base \
	kernel-debug-vmlinux \
	kernel-debug-image \
	kernel-debug-dev \
	kernel-debug-modules \
	kernel-module-versioning \
	kernel-debug-devicetree \
	kernel-debug-image-bzimage \
"
