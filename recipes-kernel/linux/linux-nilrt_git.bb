DESCRIPTION = "Linux kernel based on nilrt branch"
NI_RELEASE_VERSION = "master"
LINUX_VERSION = "5.10"
LINUX_VERSION_xilinx-zynq = "4.14"
LINUX_VERSION_qemu-zynq7 = "4.14"

require linux-nilrt.inc

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
SRCREV_xilinx-zynq = "e167d2ccda8e812cd7aa99fefc59269c854952ce"
SRCREV_qemu-zynq7 = "e167d2ccda8e812cd7aa99fefc59269c854952ce"
