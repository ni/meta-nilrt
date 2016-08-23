DESCRIPTION = "Minimal ptest image for NI Linux Real-Time x64 and ARM targets"
LICENSE = "MIT"

require minimal-nilrt-image.bb

IMAGE_FEATURES+=" ptest-pkgs"

IMAGE_INSTALL += "\
	packagegroup-ni-ptest \
"

IMAGE_ROOTFS_EXTRA_SPACE = "1000000"
