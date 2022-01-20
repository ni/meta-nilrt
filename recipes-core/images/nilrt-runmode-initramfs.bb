DESCRIPTION = "NI Linux RT runmode initramfs"
LICENSE = "MIT"

IMAGE_FSTYPES_append += " cpio.gz"

PACKAGE_INSTALL = "init-runmode-ramfs"

BAD_RECOMMENDATIONS += "shared-mime-info ca-certificates"

require includes/nilrt-core-image.inc
