DESCRIPTION ?= "Small initramfs for booting NILRT NXG targets"
LICENSE = "MIT"

PACKAGE_INSTALL = "init-nilrt-ramfs"

BAD_RECOMMENDATIONS += "shared-mime-info ca-certificates"

require includes/nilrt-image-common.inc
