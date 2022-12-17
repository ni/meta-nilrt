DESCRIPTION = "NI LinuxRT Base System Image for x64 Targets"

IMAGE_FEATURES += "x11"

# boot management
IMAGE_INSTALL = "\
"

# user software
IMAGE_INSTALL += "\
	packagegroup-ni-runmode \
	packagegroup-ni-wifi \
	packagegroup-ni-xfce \
"

# kernel software
IMAGE_INSTALL += "\
	dkms \
"

require includes/nilrt-image-base.inc
require includes/nilrt-initramfs.inc

IMAGE_FSTYPES += "squashfs"
