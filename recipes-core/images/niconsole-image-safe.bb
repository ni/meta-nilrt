DESCRIPTION = "Safemode image for ethernet based, console only, NI Linux Realtime targets."

IMAGE_INSTALL = "\
	packagegroup-ni-base \
	packagegroup-ni-tzdata \
	packagegroup-ni-wifi \
	"

require niconsole-image.inc
