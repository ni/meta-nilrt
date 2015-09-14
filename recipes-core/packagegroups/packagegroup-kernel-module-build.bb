SUMMARY = "Packages for building external kernel modules"
LICENSE = "MIT"
PR = "r1"

inherit packagegroup

RDEPENDS_${PN} = "\
	binutils \
	gcc \
	gcc-symlinks \
	kernel-dev \
	make \
	"
