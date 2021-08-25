DESCRIPTION = "NILRT linux kernel debug build"
NI_RELEASE_VERSION = "master"
LINUX_VERSION = "5.10"
LINUX_VERSION_xilinx-zynq = "4.14"
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
FILES_${KERNEL_PACKAGE_NAME}-vmlinux_remove = "/boot/vmlinux-${KERNEL_VERSION_NAME}"
FILES_${KERNEL_PACKAGE_NAME}-vmlinux += "${nonarch_base_libdir}/modules/${KERNEL_VERSION}/build/vmlinux-${KERNEL_VERSION_NAME}"

do_install_append(){
    install -d "${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/build"
    mv ${D}/boot/vmlinux-${KERNEL_VERSION} "${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/build/"
}

