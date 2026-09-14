DESCRIPTION = "NILRT safemode kernel for ARM targets"
NI_RELEASE_VERSION = "master"
LINUX_VERSION:xilinx-zynq = "4.14"
COMPATIBLE_MACHINE = "xilinx-zynq"
LINUX_KERNEL_TYPE = "safemode"

require linux-nilrt-alternate.inc

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
#SRCREV = ""
