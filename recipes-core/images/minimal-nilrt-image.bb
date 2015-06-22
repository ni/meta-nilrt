DESCRIPTION = "Minimal image for NI Linux Real-Time x64 and ARM targets"
LICENSE = "MIT"

IMAGE_PREPROCESS_COMMAND = "rootfs_update_timestamp"

IMAGE_INSTALL = "\
	packagegroup-ni-base \
	packagegroup-ni-tzdata \
	${ni_external_components} \
    "

IMAGE_FSTYPES = "tar.bz2"

require include/niconsole-image.inc

require include/ni-external-components.inc
