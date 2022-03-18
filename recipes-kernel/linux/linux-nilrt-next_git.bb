DESCRIPTION = "NILRT linux kernel next development build"
NI_RELEASE_VERSION = "21.8"
LINUX_VERSION = "5.15"
LINUX_KERNEL_TYPE = "next"

require linux-nilrt-alternate.inc

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
SRCREV = "2360492e22b4a4b78066afaaf52d9b0428ded8eb"
