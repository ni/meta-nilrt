DESCRIPTION = "Linux kernel, debug build, based on nilrt branch"

require linux-nilrt.inc

# This is an extra kernel package; zero out the PROVIDES set in kernel.bbclass
# to avoid errors related to multiple recipes providing virtual/kernel
PROVIDES = ""

NI_RELEASE_VERSION = "comms-2.0"
LINUX_VERSION = "4.1"
LINUX_VERSION_EXTENSION = "-debug"
KBRANCH = "nilrt/${NI_RELEASE_VERSION}/${LINUX_VERSION}"

KERNEL_PACKAGE_NAME = "kernel${LINUX_VERSION_EXTENSION}"

# Subfolder of the same name will be added to FILESEXTRAPATHS and also
# used for nilrt-specific config fragment manipulation during build.
# Provide a unique name for each recipe saved in the same source folder.
KBUILD_FRAGMENTS_LOCATION := "nilrt-debug"

SRC_URI += "file://debug.cfg \
           "
