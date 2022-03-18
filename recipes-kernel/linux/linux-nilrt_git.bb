DESCRIPTION = "Linux kernel based on nilrt branch"
NI_RELEASE_VERSION = "21.8"
LINUX_VERSION = "5.10"
LINUX_VERSION_xilinx-zynq = "4.14"

require linux-nilrt.inc

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
SRCREV = "eac277bc99b4925ef52b234eb6cbe48ddf55981b"
SRCREV_xilinx-zynq = "051c9569fc919a173fbc7a56c75efdbba3b13b8c"
