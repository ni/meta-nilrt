DESCRIPTION = "Linux kernel based on nilrt branch"

require linux-nilrt.inc

NI_RELEASE_VERSION = "15.0"
LINUX_VERSION = "3.14"
LINUX_VERSION_EXTENSION = "-nilrt"
KBRANCH = "nilrt_pub/${NI_RELEASE_VERSION}/${LINUX_VERSION}"

# Subfolder of the same name will be added to FILESEXTRAPATHS and also
# used for nilrt-specific defconfig manipulation during build. Provide
# a unique name for each recipe saved in the same source folder.
DEFCONFIG_LOCATION = "nilrt"
