DESCRIPTION = "NILRT linux kernel next development build"
NI_RELEASE_VERSION = "master"
LINUX_VERSION = "5.15"
LINUX_KERNEL_TYPE = "next"

require linux-nilrt-alternate.inc

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
#SRCREV = ""
