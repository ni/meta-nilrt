DESCRIPTION = "NILRT linux kernel debug build"

require linux-nilrt.inc
require linux-nilrt-squashfs.inc

SRC_URI += "\
	file://debug.cfg \
"

# This is an extra kernel package; zero out the PROVIDES set in kernel.bbclass
# to avoid errors related to multiple recipes providing virtual/kernel
PROVIDES = ""

NI_RELEASE_VERSION = "17.0"
LINUX_VERSION = "4.6"
LINUX_VERSION_EXTENSION = "-debug"
KBRANCH = "nilrt/${NI_RELEASE_VERSION}/${LINUX_VERSION}"

KERNEL_PACKAGE_NAME = "kernel${LINUX_VERSION_EXTENSION}"

# Subfolder of the same name will be added to FILESEXTRAPATHS and also
# used for nilrt-specific config fragment manipulation during build.
# Provide a unique name for each recipe saved in the same source folder.
KBUILD_FRAGMENTS_LOCATION := "nilrt-debug"

KERNEL_MODULES_META_PACKAGE = "kernel-modules-debug"
KERNEL_MODULE_PACKAGE_NAME_PREPEND = "kernel-module-debug"
KERNEL_MODULE_PACKAGE_PREPEND = "${KERNEL_MODULE_PACKAGE_NAME_PREPEND}-%s"

# Force creation of symlink to target file at a relative path
KERNEL_IMAGE_SYMLINK_DEST = "."

# Remove unecessary packages (kernel-vmlinux, kernel-dev) for optional kernel
PACKAGES = "kernel kernel-base kernel-image ${KERNEL_MODULES_META_PACKAGE}"
PKG_kernel = "kernel-debug"

# Remove kernel firmware package (refer to kernel.bbclass)
PACKAGESPLITFUNCS_remove = "split_kernel_packages"
