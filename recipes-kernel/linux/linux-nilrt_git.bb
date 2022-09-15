DESCRIPTION = "Linux kernel based on nilrt branch"
NI_RELEASE_VERSION = "22.8"
LINUX_VERSION = "5.15"
LINUX_VERSION_xilinx-zynq = "4.14"

require linux-nilrt.inc

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
SRCREV = "378d8fbcc32082ee008a0df967ebd0f41a182877"
SRCREV_xilinx-zynq = "bc0d982eacfb9764302e888ea6d9f3d3bb4a5cc9"