DESCRIPTION = "NI Linux RT runmode initramfs"
LICENSE = "MIT"

PACKAGE_INSTALL = "init-runmode-ramfs"

BAD_RECOMMENDATIONS += "shared-mime-info ca-certificates"

require includes/nilrt-core-image.inc
