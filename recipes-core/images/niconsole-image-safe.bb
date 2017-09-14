DESCRIPTION = "Safemode image for ethernet based, console only, NI Linux Realtime targets."

IMAGE_INSTALL = "\
	packagegroup-ni-base \
	packagegroup-ni-tzdata \
	packagegroup-ni-wifi \
	"

IMAGE_FSTYPES = "tar.bz2"

require niconsole-image.inc
