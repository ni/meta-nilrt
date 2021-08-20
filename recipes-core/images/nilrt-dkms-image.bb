DESCRIPTION = "Runmode image for ethernet-based NI Linux Real-Time targets running XFCE (DKMS)."

IMAGE_INSTALL = "\
	packagegroup-ni-runmode \
	packagegroup-ni-wifi \
	dkms \
	"

require niconsole-image.inc
require nilrt-xfce.inc
require nilrt-initramfs.inc
require include/licenses.inc
require nilrt-proprietary.inc

IMAGE_INSTALL_NODEPS += "\
	${NI_PROPRIETARY_BASE_PACKAGES} \
	${NI_PROPRIETARY_RUNMODE_PACKAGES} \
"

IMAGE_FSTYPES += "squashfs tar.gz"
