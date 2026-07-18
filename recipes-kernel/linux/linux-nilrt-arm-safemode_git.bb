DESCRIPTION = "NILRT safemode itb for ARM targets"
NI_RELEASE_VERSION = "master"
LINUX_VERSION:xilinx-zynq = "6.18"
COMPATIBLE_MACHINE = "xilinx-zynq"

require linux-nilrt-alternate.inc

INITRAMFS_IMAGE = "nilrt-safemode-initramfs"
FIT_DESC = "zynq_safemode - ${BUILDNAME}"
FIT_VERSION = "${@d.getVar('BUILDNAME').split('-', 1)[0]}"
FIT_DEVICECODE = "0x${@d.getVar('NILRT_ARM_DEVICE_CODES').split()[0]}"
FIT_DEVICECODES = "${@' '.join('0x' + x for x in (d.getVar('NILRT_ARM_DEVICE_CODES')).split())}"

kernel_do_deploy:append() {
    # Create a symlink that's useful to identify the correct fitImage and is also shorter.
    ln -snf fitImage-${INITRAMFS_IMAGE_NAME}-${KERNEL_FIT_NAME}${KERNEL_FIT_BIN_EXT} "$deployDir/linux_safemode.itb"
}

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
#SRCREV = ""
