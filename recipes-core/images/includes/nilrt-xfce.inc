# Mix-in of common deps to include XFCE in CG

IMAGE_INSTALL:append:x64 = "\
	packagegroup-ni-graphical \
	"

IMAGE_FEATURES:append:x64 = " x11"
