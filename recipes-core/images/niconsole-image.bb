DESCRIPTION = "Runmode image for ethernet based, console only, NI Linux Realtime targets."

IMAGE_INSTALL = "\
	packagegroup-ni-base \
	packagegroup-ni-tzdata \
	packagegroup-ni-runmode \
	packagegroup-ni-wifi \
	"

require niconsole-image.inc
require nikms-image.inc
require include/licenses.inc
