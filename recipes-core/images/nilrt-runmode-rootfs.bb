DESCRIPTION = "NI Linux RT runmode rootfs archive"


# ==============================================================================
# RECIPE VARIABLES
# ==============================================================================

DEPENDS += "nilrt-runmode-initramfs"

PV = "${DISTRO_VERSION}"


# ==============================================================================
# SOFTWARE DISTRIBUTION
# ==============================================================================

IMAGE_INSTALL = "\
	packagegroup-ni-runmode \
	packagegroup-ni-wifi \
	dkms \
"

IMAGE_INSTALL:append:x64 = "\
	nilrt-grub-runmode \
"

IMAGE_INSTALL:append:xilinx-zynq = "\
	linux-nilrt-fitimage \
"

IMAGE_INSTALL_NODEPS += "\
	${NI_PROPRIETARY_COMMON_PACKAGES} \
	${NI_PROPRIETARY_RUNMODE_PACKAGES} \
"


# ==============================================================================
# IMAGE HERITAGE
# ==============================================================================

require includes/nilrt-image-base.inc
require includes/nilrt-xfce.inc
require includes/nilrt-proprietary.inc


# ==============================================================================
# TASKS
# ==============================================================================

# Install the bootimage.ini datafile.
install_bootimage () {
	install -m 0644 \
		"${THISDIR}/files/bootimage.ini" \
		"${IMAGE_ROOTFS}/boot/runmode/bootimage.ini"
	sed -i \
		"s/%component_version%/${BUILDNAME}/" \
		"${IMAGE_ROOTFS}/boot/runmode/bootimage.ini"
}
ROOTFS_POSTPROCESS_COMMAND += " install_bootimage; "


# FIXUP KERNEL #

RAMDISK_IMAGE = "nilrt-runmode-initramfs"
BOOT_TMP_PATH = "/boot/tmp"

# Install the kernel and ramdisk to a temporary path, so that the BSI postinst
# can move them later.
fixup_kernel_x64 () {
	install -d "${IMAGE_ROOTFS}/${BOOT_TMP_PATH}"
	# kernel
	mv \
		"${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}" \
		"${IMAGE_ROOTFS}/${BOOT_TMP_PATH}/runmode"
	# ramdisk
	install -m 0644 \
		"${DEPLOY_DIR_IMAGE}/${RAMDISK_IMAGE}-${MACHINE}.cpio.xz" \
		"${IMAGE_ROOTFS}/${BOOT_TMP_PATH}/runmode/ramdisk.xz"
}
fixup_kernel_x64[depends] += "${RAMDISK_IMAGE}:do_image_complete"
ROOTFS_POSTPROCESS_COMMAND:append:x64 = " fixup_kernel_x64; "


# Give the ARMv7 runmode ITB a distinct name from the safemode ITB.
fixup_kernel_armv7a () {
	# Stage the ITB under boot/runmode/ rather than directly in boot/.
	# This prevents u-boot from attempting to boot a partially-extracted
	# runmode installation: if BSI extraction fails (e.g. disk full), postinst
	# never runs, so the ITB is never moved to its final /boot location and
	# u-boot will correctly treat runmode as not installed.
	install -d "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/runmode"
	mv "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/fitImage" "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/runmode/linux_runmode.itb"
	find ${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST} -maxdepth 1 -type f \
		! -name 'linux_runmode.itb' \
		-exec rm -f {} +
}
ROOTFS_POSTPROCESS_COMMAND:append:armv7a = " fixup_kernel_armv7a; "

# /FIXUP KERNEL #


# ==============================================================================
# DEPLOYMENT
# ==============================================================================

IMAGE_FSTYPES += "squashfs ${NILRT_BSI_FSTYPE}"
