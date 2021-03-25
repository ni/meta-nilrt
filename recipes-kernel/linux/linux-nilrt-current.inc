NI_RELEASE_VERSION = "20.7"
LINUX_VERSION = "4.14"
KBRANCH = "nilrt/${NI_RELEASE_VERSION}/${LINUX_VERSION}"
GIT_KERNEL_REPO = "linux.git"

require linux-nilrt.inc

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
#SRCREV = ""
