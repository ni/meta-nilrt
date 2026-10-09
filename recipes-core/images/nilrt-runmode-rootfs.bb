DESCRIPTION = "NI Linux RT runmode rootfs archive"

SECURE_BOOT_ENABLED = "${@bb.utils.contains('DISTRO_FEATURES', 'efi-secure-boot', '1', '0', d)}"

inherit ${@bb.utils.contains('DISTRO_FEATURES', 'efi-secure-boot', 'user-key-store', '', d)}

SRC_URI += "\
	file://bootimage.ini \
"

IMAGE_INSTALL = "\
	packagegroup-ni-runmode \
	packagegroup-ni-wifi \
	dkms \
"

IMAGE_INSTALL:append:x64 = "\
	nilrt-grub-runmode \
	grub-bootconf-nilrt \
	${@bb.utils.contains('DISTRO_FEATURES', 'efi-secure-boot', 'packagegroup-efi-secure-boot', '', d)} \
	"

require includes/nilrt-image-base.inc
require includes/nilrt-xfce.inc
require includes/nilrt-proprietary.inc

IMAGE_INSTALL_NODEPS += "\
	${NI_PROPRIETARY_COMMON_PACKAGES} \
	${NI_PROPRIETARY_RUNMODE_PACKAGES} \
"

# Ensure that rauc does not end up in this image.
PACKAGE_EXCLUDE += "rauc rauc-mark-good"

# on older NILRT distro flavors the kernel is installed in non-standard paths
# for backward compatibility
CUSTOM_KERNEL_PATH:x64 ?= "/boot/tmp/runmode"

bootimg_fixup_x64() {
	install -d "${IMAGE_ROOTFS}/boot/runmode"
	install -m 0644 "${THISDIR}/files/bootimage.ini" "${IMAGE_ROOTFS}/boot/runmode/bootimage.ini"
	sed -i "s/%component_version%/${BUILDNAME}/" "${IMAGE_ROOTFS}/boot/runmode/bootimage.ini"

	# Postinst script is going to want this all in /boot/tmp/runmode
	install -d `dirname "${IMAGE_ROOTFS}/${CUSTOM_KERNEL_PATH}"`
	mv "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}" "${IMAGE_ROOTFS}/${CUSTOM_KERNEL_PATH}"
}

ensure_secure_boot_kernel_sidecar_x64() {
	if [ "${SECURE_BOOT_ENABLED}" != "1" ]; then
		return
	fi

	if [ -z "${SB_FILE_EXT}" ]; then
		echo "ERROR: SB_FILE_EXT is not set while secure boot is enabled." 1>&2
		exit 1
	fi

	if [ ! -e "${IMAGE_ROOTFS}/${CUSTOM_KERNEL_PATH}/bzImage${SB_FILE_EXT}" ]; then
		echo "ERROR: ${CUSTOM_KERNEL_PATH}/bzImage${SB_FILE_EXT} is required in secure-boot runmode rootfs image." 1>&2
		exit 1
	fi
}

python secure_boot_runmode_sign() {
    import glob
    import os

    if d.getVar('SECURE_BOOT_ENABLED') != '1':
        return

    if not d.getVar('SB_FILE_EXT'):
        bb.fatal('SB_FILE_EXT is not set while secure boot is enabled.')

    rootfs = d.getVar('IMAGE_ROOTFS')
    staged_runmode = rootfs + d.getVar('CUSTOM_KERNEL_PATH')
    targets = [staged_runmode + '/bootimage.cfg']
    targets += sorted(glob.glob(staged_runmode + '/bootimage.cfg.d/*.cfg'))

    for target in targets:
        if not os.path.exists(target):
            bb.fatal('%s is required in secure-boot runmode image but is missing.'
                     % target[len(rootfs):])
        uks_bl_sign(target, d)

    # Sourced by bootimage.cfg, so SELoader verifies it too.
    mitigations = staged_runmode + '/cpu-mitigations.cfg'
    if os.path.exists(mitigations):
        uks_bl_sign(mitigations, d)
}
secure_boot_runmode_sign[prefuncs] += "check_deploy_keys"

bootimg_fixup_arm() {
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

IMAGE_PREPROCESS_COMMAND:append:x64 = " bootimg_fixup_x64 ensure_secure_boot_kernel_sidecar_x64 secure_boot_runmode_sign"
IMAGE_PREPROCESS_COMMAND:append:xilinx-zynq = " bootimg_fixup_arm"

IMAGE_FSTYPES += "squashfs ${NILRT_BSI_FSTYPE}"
