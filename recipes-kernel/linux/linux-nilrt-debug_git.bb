DESCRIPTION = "NILRT linux kernel debug build"
NI_RELEASE_VERSION = "23.8"
LINUX_VERSION = "6.1"
LINUX_VERSION:xilinx-zynq = "4.14"
LINUX_KERNEL_TYPE = "debug"

require linux-nilrt-alternate.inc

SRC_URI += "\
	file://debug.cfg \
"

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
#SRCREV = ""

# Move vmlinux-${KERNEL_VERSION_NAME} from /boot to /lib/modules/${KERNEL_VERSION}/build/
# to avoid filling the /boot partition.
VMLINUX_DIR = "${nonarch_base_libdir}/modules/${KERNEL_VERSION}/build"
FILES:${KERNEL_PACKAGE_NAME}-vmlinux:remove = "/boot/vmlinux-${KERNEL_VERSION_NAME}"
FILES:${KERNEL_PACKAGE_NAME}-vmlinux += "${VMLINUX_DIR}/vmlinux-${KERNEL_VERSION_NAME}"

do_install:append(){
    install -d "${D}/${VMLINUX_DIR}"
    mv ${D}/${KERNEL_IMAGEDEST}/vmlinux-${KERNEL_VERSION} "${D}/${VMLINUX_DIR}/"
}

