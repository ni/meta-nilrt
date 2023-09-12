DESCRIPTION = "NILRT linux kernel full dynamic ticks (NO_HZ_FULL) build"
NI_RELEASE_VERSION = "23.8"
LINUX_VERSION = "6.1"
LINUX_KERNEL_TYPE = "nohz"

require linux-nilrt-alternate.inc

SRC_URI += "\
	file://no_hz_full.cfg \
	file://README.nohz \
"

FILES:${KERNEL_PACKAGE_NAME} += "${docdir}/*"

do_install:append() {
        install -d -m 755 ${D}${docdir}/${KERNEL_PACKAGE_NAME}
        install -p -m 644 ${WORKDIR}/README.nohz ${D}${docdir}/${KERNEL_PACKAGE_NAME}/
}

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
#SRCREV = ""
