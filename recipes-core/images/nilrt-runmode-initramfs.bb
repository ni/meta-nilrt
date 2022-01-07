DESCRIPTION ?= "initramfs for NI LinuxRT runmode images"
LICENSE = "MIT"

PACKAGE_INSTALL = "init-runmode-ramfs"

BAD_RECOMMENDATIONS += "shared-mime-info ca-certificates"

require nilrt-image-common.inc
