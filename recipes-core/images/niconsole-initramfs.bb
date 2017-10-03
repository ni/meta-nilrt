DESCRIPTION ?= "Small initramfs for booting older NILRT runmode targets"
LICENSE = "MIT"

PACKAGE_INSTALL = "init-runmode-ramfs mkinitcpio"

require nilrt-image-common.inc

IMAGE_FSTYPES += "cpio.gz"
