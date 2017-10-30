DESCRIPTION = "Minimal image for NI Linux Real-Time x64 and ARM targets"
LICENSE = "MIT"

IMAGE_INSTALL = "\
	packagegroup-ni-base \
	packagegroup-ni-tzdata \
    "

IMAGE_FSTYPES_append += "ext2"

require niconsole-image.inc

require include/ni-external-components.inc
