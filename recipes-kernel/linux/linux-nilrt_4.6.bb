DESCRIPTION = "Linux kernel based on nilrt branch"

require linux-nilrt.inc

NI_RELEASE_VERSION = "17.1"
LINUX_VERSION = "4.6"
LINUX_VERSION_EXTENSION = "-nilrt"
KBRANCH = "nilrt/${NI_RELEASE_VERSION}/${LINUX_VERSION}"
KBRANCH_n310 = "dev/bmouring/nilrt/cardassia/${LINUX_VERSION}"

# Subfolder of the same name will be added to FILESEXTRAPATHS and also
# used for nilrt-specific config fragment manipulation during build.
# Provide a unique name for each recipe saved in the same source folder.
KBUILD_FRAGMENTS_LOCATION = "nilrt"
