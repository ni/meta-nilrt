DESCRIPTION = "NI Linux RT runmode - slimmed - container image"

IMAGE_INSTALL = "\
	packagegroup-ni-runmode \
"

require includes/nilrt-image-base.inc

require includes/nilrt-container.inc

OCI_IMAGE_TAG = "${DISTRO_VERSION}-slim"
