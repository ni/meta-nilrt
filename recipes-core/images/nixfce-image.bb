DESCRIPTION = "Runmode image for ethernet-based NI Linux Real-Time targets running XFCE."

IMAGE_PREPROCESS_COMMAND = "rootfs_update_timestamp; "

IMAGE_INSTALL = "\
	packagegroup-ni-base \
	packagegroup-ni-tzdata \
	packagegroup-ni-runmode \
	packagegroup-ni-wifi \
	packagegroup-ni-xfce \
	"

IMAGE_FSTYPES = "tar.bz2"

require include/niconsole-image.inc

IMAGE_FEATURES += "x11"
