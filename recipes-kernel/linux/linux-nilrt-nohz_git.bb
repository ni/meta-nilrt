DESCRIPTION = "NILRT linux kernel full dynamic ticks (NO_HZ_FULL) build"
NI_RELEASE_VERSION = "master"
LINUX_VERSION = "5.10"
LINUX_KERNEL_TYPE = "nohz"

require linux-nilrt-alternate.inc

SRC_URI += "\
	file://no_hz_full.cfg \
"

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
SRCREV = "97d3a49560d995aed5ee8d4e3a800aca43f327d4"
