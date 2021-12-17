DESCRIPTION = "NILRT linux kernel next development build"
NI_RELEASE_VERSION = "21.5"
LINUX_VERSION = "5.15"
LINUX_KERNEL_TYPE = "next"

require linux-nilrt-alternate.inc

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
SRCREV = "3c141e5e7a0719a4eb9ef0301ae4b41450526560"
