DESCRIPTION = "Minimal ptest image for NI Linux Real-Time x64 and ARM targets"
LICENSE = "MIT"

require minimal-nilrt-image.bb

IMAGE_INSTALL += "\
	packagegroup-ni-ptest \
"

IMAGE_ROOTFS_EXTRA_SPACE = "1000000"

rm_feed_configs () {
    ${ROOTFS_PKGMANAGE} -o ${IMAGE_ROOTFS} remove --force-depends distro-feed-configs
}

IMAGE_PREPROCESS_COMMAND =+ "rm_feed_configs; "
