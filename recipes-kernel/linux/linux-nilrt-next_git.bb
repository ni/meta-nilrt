DESCRIPTION = "NILRT linux kernel next development build"

require linux-nilrt-next.inc

KERNEL_PACKAGE_NAME = "kernel-next"

# Subfolder of the same name will be added to FILESEXTRAPATHS and also
# used for nilrt-specific config fragment manipulation during build.
# Provide a unique name for each recipe saved in the same source folder.
KBUILD_FRAGMENTS_LOCATION := "nilrt-next"

# Force creation of symlink to target file at a relative path
KERNEL_IMAGE_SYMLINK_DEST = "."
