DESCRIPTION = "NI LinuxRT Base System Image for x64 Targets"

IMAGE_FEATURES += "x11"

# boot management
IMAGE_INSTALL = "\
	rauc \
	rauc-mark-good \
"

# user software
IMAGE_INSTALL += "\
	packagegroup-ni-runmode \
	packagegroup-ni-transconf \
	packagegroup-ni-wifi \
	packagegroup-ni-xfce \
"

# kernel software
IMAGE_INSTALL += "\
	dkms \
"

require includes/niconsole-image.inc
require includes/nilrt-initramfs.inc
require includes/licenses.inc

IMAGE_FSTYPES += "squashfs"
