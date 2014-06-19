# mark hold packages
ROOTFS_POSTPROCESS_COMMAND += "opkg-cl ${IPKG_ARGS} flag hold libc6; "

inherit core-image

# without package-management update-rc.d gets removed from image
IMAGE_FEATURES += "package-management"
