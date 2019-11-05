DESCRIPTION = "Runmode image for ethernet based, console only, NI Linux Realtime targets (DKMS)."

IMAGE_INSTALL = "\
	packagegroup-ni-runmode \
	packagegroup-ni-wifi \
	dkms \
	"

require niconsole-image.inc
require include/licenses.inc
