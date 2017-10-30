DESCRIPTION = "Runmode image for ethernet-based NI Linux Real-Time targets running XFCE."

IMAGE_INSTALL = "\
	packagegroup-ni-base \
	packagegroup-ni-tzdata \
	packagegroup-ni-runmode \
	packagegroup-ni-wifi \
	packagegroup-ni-xfce \
	"

require niconsole-image.inc
require include/licenses.inc

IMAGE_FEATURES += "x11"
