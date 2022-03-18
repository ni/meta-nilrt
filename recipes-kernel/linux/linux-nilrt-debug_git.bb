DESCRIPTION = "NILRT linux kernel debug build"
NI_RELEASE_VERSION = "21.8"
LINUX_VERSION = "5.10"
LINUX_VERSION_xilinx-zynq = "4.14"
LINUX_KERNEL_TYPE = "debug"

require linux-nilrt-alternate.inc

SRC_URI += "\
	file://debug.cfg \
"

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
SRCREV = "eac277bc99b4925ef52b234eb6cbe48ddf55981b"
SRCREV_xilinx-zynq = "051c9569fc919a173fbc7a56c75efdbba3b13b8c"

# Move vmlinux-${KERNEL_VERSION_NAME} from /boot to /lib/modules/${KERNEL_VERSION}/build/
# to avoid filling the /boot partition.
VMLINUX_DIR = "${nonarch_base_libdir}/modules/${KERNEL_VERSION}/build"
FILES_${KERNEL_PACKAGE_NAME}-vmlinux_remove = "/boot/vmlinux-${KERNEL_VERSION_NAME}"
FILES_${KERNEL_PACKAGE_NAME}-vmlinux += "${VMLINUX_DIR}/vmlinux-${KERNEL_VERSION_NAME}"

do_install_append(){
    install -d "${D}/${VMLINUX_DIR}"
    mv ${D}/boot/vmlinux-${KERNEL_VERSION} "${D}/${VMLINUX_DIR}/"
}

