DESCRIPTION = "Linux kernel based on nilrt branch"
NI_RELEASE_VERSION = "21.0"
LINUX_VERSION = "4.14"

require linux-nilrt.inc

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
SRCREV = "38d90060d000abc261085c3f4d398ec5606e9efc"
