DESCRIPTION = "NI Linux RT runmode container image"

IMAGE_INSTALL = "\
	packagegroup-ni-runmode \
	packagegroup-ni-wifi \
	dkms \
	nilrt-grub-runmode \
	env-config-container \
	"

require includes/nilrt-image-base.inc
require includes/nilrt-proprietary.inc

IMAGE_INSTALL_NODEPS += "\
	${NI_PROPRIETARY_COMMON_PACKAGES} \
	${NI_PROPRIETARY_RUNMODE_PACKAGES} \
"

require includes/nilrt-container.inc
