DESCRIPTION = "Runmode image for ethernet based, console only, NI Linux Realtime targets."

IMAGE_INSTALL = "\
	packagegroup-ni-runmode \
	packagegroup-ni-wifi \
	"

require niconsole-image.inc
require nilrt-nikms.inc
require include/licenses.inc
