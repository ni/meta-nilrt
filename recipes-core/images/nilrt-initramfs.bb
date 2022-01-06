DESCRIPTION ?= "Initramfs for booting NI LinuxRT"
LICENSE = "MIT"

PACKAGE_INSTALL = "init-runmode-ramfs"

BAD_RECOMMENDATIONS += "shared-mime-info ca-certificates"

require nilrt-image-common.inc
