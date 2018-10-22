DESCRIPTION ?= "Base image setup used in all NILRT images"
LICENSE = "MIT"

IMAGE_FSTYPES = "${INITRAMFS_FSTYPES} tar.bz2 ext2"

IMAGE_FEATURES ??= ""
IMAGE_LINGUAS ??= ""

IMAGE_PREPROCESS_COMMAND += "rootfs_update_timestamp;"

inherit core-image
