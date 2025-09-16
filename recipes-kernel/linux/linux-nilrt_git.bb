DESCRIPTION = "Linux kernel based on nilrt branch"
NI_RELEASE_VERSION = "25.8"
LINUX_VERSION = "6.12"
LINUX_VERSION:xilinx-zynq = "4.14"

require linux-nilrt.inc

SRC_URI:append:x64 = "\
	file://extra_x64.cfg \
"

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
#SRCREV = ""
