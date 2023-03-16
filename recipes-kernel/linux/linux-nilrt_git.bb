DESCRIPTION = "Linux kernel based on nilrt branch"
NI_RELEASE_VERSION = "23.3"
LINUX_VERSION = "5.15"
LINUX_VERSION:xilinx-zynq = "4.14"

require linux-nilrt.inc

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
#SRCREV = ""
