DESCRIPTION ?= "Small initramfs for booting NILRT NXG targets"
LICENSE = "MIT"

PACKAGE_INSTALL = "init-nilrt-ramfs"

BAD_RECOMMENDATIONS += "shared-mime-info ca-certificates"

require nilrt-image-common.inc
