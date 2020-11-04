DESCRIPTION = "Runmode image for ethernet-based NI Linux Real-Time targets running XFCE (DKMS)."

IMAGE_INSTALL = "\
	packagegroup-ni-runmode \
	packagegroup-ni-wifi \
	dkms \
	"

require niconsole-image.inc
require nilrt-xfce.inc
require nilrt-initramfs-legacy.inc
require include/licenses.inc
require nilrt-proprietary.inc

IMAGE_FSTYPES += "squashfs"
