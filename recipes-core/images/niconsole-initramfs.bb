DESCRIPTION ?= "Small initramfs for booting older NILRT runmode targets"
LICENSE = "MIT"

PACKAGE_INSTALL = "		\
	util-linux-switch-root	\
	cryptsetup		\
	dash			\
	mkinitcpio		\
"

require nilrt-image-common.inc
