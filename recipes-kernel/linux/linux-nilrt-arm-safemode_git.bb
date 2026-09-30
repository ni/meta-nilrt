DESCRIPTION = "NILRT safemode kernel for ARM targets"
NI_RELEASE_VERSION = "master"
LINUX_VERSION:xilinx-zynq = "6.18"
COMPATIBLE_MACHINE = "xilinx-zynq"
LINUX_KERNEL_TYPE = "safemode"

require linux-nilrt-alternate.inc

# The safemode initramfs ships virtual/kernel's modules, so keep the defconfig's
# "-ni" LOCALVERSION instead of appending "-safemode", or they will not load.
LINUX_VERSION_EXTENSION = ""

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
#SRCREV = ""
