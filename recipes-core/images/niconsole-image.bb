DESCRIPTION = "Runmode image for ethernet based, console only, NI Linux Realtime targets."

IMAGE_INSTALL = "\
	packagegroup-ni-base \
	packagegroup-ni-tzdata \
	packagegroup-ni-runmode \
	packagegroup-ni-wifi \
	"

ROOTFS_POSTPROCESS_COMMAND += "install_module_versioning_squashfs;"

require niconsole-image.inc
require include/licenses.inc
