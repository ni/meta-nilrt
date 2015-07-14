DESCRIPTION = "Linux kernel, debug build, based on nilrt branch"

require linux-nilrt.inc
require linux-nilrt-squashfs.inc

NI_RELEASE_VERSION = "comms-2.0"
LINUX_VERSION = "3.14"
LINUX_VERSION_EXTENSION = "-nilrt-debug"
KBRANCH = "nilrt/${NI_RELEASE_VERSION}/${LINUX_VERSION}"

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

# Subfolder of the same name will be added to FILESEXTRAPATHS and also
# used for nilrt-specific config fragment manipulation during build.
# Provide a unique name for each recipe saved in the same source folder.
KBUILD_FRAGMENTS_LOCATION := "nilrt-debug"

SRC_URI += "file://debug.cfg \
           "

