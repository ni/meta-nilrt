DESCRIPTION = "Linux kernel based on nilrt branch"
NI_RELEASE_VERSION = "master"
LINUX_VERSION = "5.10"
LINUX_VERSION_xilinx-zynq = "4.14"

require linux-nilrt.inc

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
SRCREV_xilinx-zynq = "4442d176ee60b472252436950058a85fce08a3a3"
