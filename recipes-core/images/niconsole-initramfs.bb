DESCRIPTION ?= "Small initramfs for booting older NILRT runmode targets"
LICENSE = "MIT"

PACKAGE_INSTALL = "init-runmode-ramfs"

BAD_RECOMMENDATIONS += "shared-mime-info ca-certificates"

require nilrt-image-common.inc
