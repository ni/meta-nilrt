DESCRIPTION = "NILRT linux kernel next development build"
NI_RELEASE_VERSION = "22.5"
LINUX_VERSION = "5.15"
LINUX_KERNEL_TYPE = "next"

require linux-nilrt-alternate.inc

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
SRCREV = "9f2c2bbbbfc5e67656fb4bf86bef2085fefb472f"
