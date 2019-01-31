DESCRIPTION = "Minimal image for NI Linux Real-Time x64 and ARM targets"
LICENSE = "MIT"

IMAGE_INSTALL = "\
	dkms \
	"

require niconsole-image.inc

require include/ni-external-components.inc
