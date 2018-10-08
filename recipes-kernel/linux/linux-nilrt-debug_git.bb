DESCRIPTION = "NILRT linux kernel debug build"

require linux-nilrt.inc

SRC_URI += "\
	file://debug.cfg \
"

LINUX_VERSION_EXTENSION = "-debug"
KERNEL_PACKAGE_NAME = "kernel${LINUX_VERSION_EXTENSION}"

# Subfolder of the same name will be added to FILESEXTRAPATHS and also
# used for nilrt-specific config fragment manipulation during build.
# Provide a unique name for each recipe saved in the same source folder.
KBUILD_FRAGMENTS_LOCATION := "nilrt-debug"

# Force creation of symlink to target file at a relative path
KERNEL_IMAGE_SYMLINK_DEST = "."
