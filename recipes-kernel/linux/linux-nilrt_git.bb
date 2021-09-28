DESCRIPTION = "Linux kernel based on nilrt branch"
NI_RELEASE_VERSION = "21.3"
LINUX_VERSION = "5.10"
LINUX_VERSION_xilinx-zynq = "4.14"

require linux-nilrt.inc

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
SRCREV = "4de019e72df37f49e08f352a835cf5779d57fac2"
SRCREV_xilinx-zynq = "cf7239e5a7055e3a3f19b40d83347ccbe70b44e7"
