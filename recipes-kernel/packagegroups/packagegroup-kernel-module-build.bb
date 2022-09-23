SUMMARY = "Packages for building external kernel modules"
LICENSE = "MIT"

inherit packagegroup

RDEPENDS:${PN} = "\
	binutils \
	gcc \
	gcc-symlinks \
	kernel-dev \
	kernel-devsrc \
	make \
	"
