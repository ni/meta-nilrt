SUMMARY = "NILRT safemode kernel as a FIT image (with initramfs) for ARM targets"
SECTION = "kernel"

# If an initramfs is included in the FIT image more licenses apply.
# But also the kernel uses more than one license (see Documentation/process/license-rules.rst)
LICENSE = "GPL-2.0-with-Linux-syscall-note"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/GPL-2.0-with-Linux-syscall-note;md5=0bad96c422c41c3a94009dcfe1bff992"

COMPATIBLE_MACHINE = "xilinx-zynq"

inherit linux-kernel-base kernel-fit-image

# Set the version of this recipe to the version of the included kernel
# (without taking the long way around via PV)
PKGV = "${@get_kernelversion_file("${STAGING_KERNEL_BUILDDIR}")}"

# linux-nilrt-arm-safemode (not virtual/kernel) provides the deployed
# artifacts (linux.bin, linux_comp, DTBs) this recipe assembles into a FIT.
KERNEL_DEPLOYSUBDIR = "kernel-safemode"
# Since the artifacts do not come from virtual/kernel, we must declare the
# dependency here. This provides the equivalent of the similar
# do_compile[depends] line from the kernel-fit-image bbclass.
do_compile[depends] = "linux-nilrt-arm-safemode:do_deploy"

INITRAMFS_IMAGE = "nilrt-safemode-initramfs"

FIT_DESC = "zynq_safemode - ${BUILDNAME}"
FIT_VERSION = "${@d.getVar('BUILDNAME').split('-', 1)[0]}"
FIT_DEVICECODE = "0x${@d.getVar('NILRT_ARM_DEVICE_CODES').split()[0]}"
FIT_DEVICECODES = "${@' '.join('0x' + x for x in (d.getVar('NILRT_ARM_DEVICE_CODES')).split())}"

DEPENDS += "u-boot"

do_prepare_bootscript() {
    # Copy bootscript.txt from u-boot's deploy dir into our sources
    cp ${DEPLOY_DIR_IMAGE}/bootscript.txt ${UNPACKDIR}/bootscript.txt
}
addtask prepare_bootscript before do_compile after do_configure

do_install:append() {
    # Create a symlink that's useful to identify the correct fitImage and is also shorter.
    ln -snf fitImage "${D}/${KERNEL_IMAGEDEST}/linux_safemode.itb"
}

do_deploy:append() {
    # Mirror the same symlink into the deploy dir.
    ln -snf fitImage "$deploy_dir/linux_safemode.itb"
}
