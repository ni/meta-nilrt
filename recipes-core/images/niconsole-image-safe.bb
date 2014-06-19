DESCRIPTION = "Safemode image for ethernet based, console only, NI Linux Realtime targets."

IMAGE_PREPROCESS_COMMAND = "rootfs_update_timestamp"

IMAGE_INSTALL = "\
	packagegroup-ni-base \
	packagegroup-ni-tzdata \
	packagegroup-ni-wifi \
	niwatchdogpet \
	"

IMAGE_FSTYPES = "tar.bz2"

require include/enable-empty-root-passwd.inc
require include/niconsole-image.inc

BAD_RECOMMENDATIONS += "usbutils-ids"
