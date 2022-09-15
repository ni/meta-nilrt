DESCRIPTION = "NILRT linux kernel next development build"
NI_RELEASE_VERSION = "22.8"
LINUX_VERSION = "5.19"
LINUX_KERNEL_TYPE = "next"

require linux-nilrt-alternate.inc

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
SRCREV = "ebe990bf131e45cd1523d8b18406e5b49ff61bea"
