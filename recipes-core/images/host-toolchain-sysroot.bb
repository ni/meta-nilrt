DESCRIPTION = "Image used to provide the sysroot for the toolchains generated for cross-compilation"
LICENSE = "MIT"

inherit core-image

IPK_FEED_URIS=""
BUILD_IMAGES_FROM_FEEDS="0"

IMAGE_PREPROCESS_COMMAND = "rootfs_update_timestamp"

IMAGE_INSTALL = "\
	packagegroup-ni-base \
	packagegroup-ni-tzdata \
    "

