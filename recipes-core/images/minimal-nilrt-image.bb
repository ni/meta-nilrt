DESCRIPTION = "Minimal image for NI Linux Real-Time x64 and ARM targets"
LICENSE = "MIT"

IMAGE_INSTALL = "\
	dkms \
	"

require niconsole-image.inc

IMAGE_FSTYPES += "squashfs"

require include/ni-external-components.inc

do_boot_cleanup() {
	rm -rf ${IMAGE_ROOTFS}/boot/*
}

IMAGE_PREPROCESS_COMMAND += "do_boot_cleanup;"

