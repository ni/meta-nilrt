DESCRIPTION = "Runmode image for ethernet-based NI Linux Real-Time targets running XFCE."

IMAGE_INSTALL = "\
	packagegroup-ni-runmode \
	packagegroup-ni-wifi \
	"

require niconsole-image.inc
require nilrt-xfce.inc
require nilrt-nikms.inc
require nilrt-initramfs.inc
require include/licenses.inc
