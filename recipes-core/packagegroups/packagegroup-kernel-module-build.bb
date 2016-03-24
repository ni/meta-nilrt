SUMMARY = "Packages for building external kernel modules"
LICENSE = "MIT"

inherit packagegroup

RDEPENDS_${PN} = "\
	binutils \
	gcc \
	gcc-symlinks \
	kernel-dev \
	make \
	"
