DESCRIPTION = "Linux kernel based on nilrt branch"
NI_RELEASE_VERSION = "22.5"
LINUX_VERSION = "5.10"
LINUX_VERSION_xilinx-zynq = "4.14"

require linux-nilrt.inc

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
SRCREV = "ed225475754f22ca9e9f239caa6f267b33db78c1"
SRCREV_xilinx-zynq = "051c9569fc919a173fbc7a56c75efdbba3b13b8c"
