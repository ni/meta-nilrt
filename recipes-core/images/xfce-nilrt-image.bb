# Start with the minimal image as a base
require minimal-nilrt-image.bb

DESCRIPTION = "XFCE DE image for NI Linux Real-Time"

IMAGE_INSTALL += "\
	packagegroup-ni-xfce \
"
