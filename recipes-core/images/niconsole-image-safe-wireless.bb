DESCRIPTION = "Safemode image for Wi-Fi enabled, console only, NI Linux Realtime targets."

IMAGE_PREPROCESS_COMMAND = "rootfs_update_timestamp"

IMAGE_DEV_MANAGER = "busybox-mdev"

IMAGE_INSTALL = "\
	packagegroup-ni-base \
	packagegroup-ni-tzdata \
	packagegroup-ni-wifi \
	"

IMAGE_FSTYPES = "tar.bz2"

require include/niconsole-image.inc

inherit image
