DESCRIPTION = "Filesystem image/archive of NILRT boot partition containing boot loader and runmode NILRT image"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

IMAGE_FSTYPES = "tar.bz2"

SECURE_BOOT_ENABLED = "${@bb.utils.contains('DISTRO_FEATURES', 'efi-secure-boot', '1', '0', d)}"

inherit ${@bb.utils.contains('DISTRO_FEATURES', 'efi-secure-boot', 'user-key-store', '', d)}

DEPENDS = "grub-efi grub-bootconf ${PREFERRED_PROVIDER_virtual/kernel}"

SRC_URI += "file://grub-dkms-bundle.cfg"

IMAGE_INSTALL = " \
	grub-efi-nilrt \
	grub-bootconf-nilrt \
	kernel-image-bzimage \
"

INITRAMFS_IMAGE = "nilrt-initramfs"
do_rootfs[depends] += "${INITRAMFS_IMAGE}:do_image_complete"

BASEROOTFS_IMAGE = "nilrt-dkms-image"
do_rootfs[depends] += "${BASEROOTFS_IMAGE}:do_image_complete"

bootimg_fixup() {
	# Install factory image to /boot/EFI/BOOT/
	install -m 0644 "${DEPLOY_DIR_IMAGE}/${BASEROOTFS_IMAGE}-${MACHINE}.squashfs" "${IMAGE_ROOTFS}/boot/baserootfs.squashfs"

	# Install initramfs image to /boot/EFI/BOOT/
	install -m 0644 "${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE}-${MACHINE}.cpio.gz" "${IMAGE_ROOTFS}/boot/initrd.cpio.gz"

	# The shared grub-bootconf menu is for BSI runmode and expects nirootfs.
	# DKMS bundles boot from the active ESP using the supplied initramfs.
	install -m 0644 "${UNPACKDIR}/grub-dkms-bundle.cfg" "${IMAGE_ROOTFS}/boot/efi/nilrt/grub.cfg"
	if [ "${SECURE_BOOT_ENABLED}" = "1" ]; then
		rm -f "${IMAGE_ROOTFS}/boot/efi/nilrt/grub.cfg${SB_FILE_EXT}"
	fi

	# Install version file
	echo "${BUILDNAME}" >"${IMAGE_ROOTFS}/boot/version"

	# Generate readme.txt file to describe image contents
	echo  >"${IMAGE_ROOTFS}/boot/readme.txt" "${PN} ${PV} ${PR} system partition image:"
	echo >>"${IMAGE_ROOTFS}/boot/readme.txt" " - bootx64.efi: Grub EFI binary"
	echo >>"${IMAGE_ROOTFS}/boot/readme.txt" " - grub.cfg: Grub configuration"
	echo >>"${IMAGE_ROOTFS}/boot/readme.txt" " - bzImage: $(readlink "${IMAGE_ROOTFS}/boot/bzImage") kernel image"
	echo >>"${IMAGE_ROOTFS}/boot/readme.txt" " - initrd.cpio.gz: ${INITRAMFS_IMAGE}-${MACHINE}.cpio.gz ramdisk image"
	echo >>"${IMAGE_ROOTFS}/boot/readme.txt" " - baserootfs.squashfs: ${BASEROOTFS_IMAGE}-${MACHINE}.tar.bz2 root file system image"

	# Move /boot/runmode/bzImage to /boot/bzImage
	mv "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/$(readlink "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/bzImage")" "${IMAGE_ROOTFS}/boot/bzImage"
	# Remove /boot/runmode
	rm -rf "${IMAGE_ROOTFS}/boot/runmode"

	# Bitbake insists on installing glibc which is not needed on
	#  EFI system partition. Cleanup all non-boot related files.
	find "${IMAGE_ROOTFS}" -mindepth 1 \
		-not -path "${IMAGE_ROOTFS}/boot" \
		-a -not -path "${IMAGE_ROOTFS}/boot/*" \
		-a -not -path "${IMAGE_ROOTFS}/boot/efi" \
		-a -not -path "${IMAGE_ROOTFS}/boot/efi/nilrt" \
		-a -not -path "${IMAGE_ROOTFS}/boot/efi/nilrt/*" \
		-a -not -path "${IMAGE_ROOTFS}/boot/bootimage.cfg.d" \
		-a -not -path "${IMAGE_ROOTFS}/boot/bootimage.cfg.d/*" \
		-delete

	# Promote EFI system directory to top so that filesystem looks
	# like this afterwards:
	#  bzImage
	#  baserootfs.squashfs
	#  initrd.cpio.gz
	#  EFI
	#    BOOT
	#      bootx64.bin
	#      grub.cfg
	mv "${IMAGE_ROOTFS}"/boot/* "${IMAGE_ROOTFS}/"
	rmdir "${IMAGE_ROOTFS}/boot"
}

python secure_boot_esp_sign() {
    import glob
    import os

    if d.getVar('SECURE_BOOT_ENABLED') != '1':
        return

    bb.build.exec_func('check_deploy_keys', d)
    if d.getVar('GRUB_SIGN_VERIFY') == '1':
        bb.build.exec_func('check_boot_public_key', d)

    rootfs = d.getVar('IMAGE_ROOTFS')
    targets = [rootfs + '/efi/nilrt/grub.cfg',
               rootfs + '/bzImage',
               rootfs + '/initrd.cpio.gz']
    targets += sorted(glob.glob(rootfs + '/bootimage.cfg.d/*.cfg'))

    for target in targets:
        if not os.path.exists(target):
            bb.fatal('%s is required in the DKMS ESP image but is missing.'
                     % target[len(rootfs):])
        uks_bl_sign(target, d)
}

IMAGE_PREPROCESS_COMMAND += " bootimg_fixup; secure_boot_esp_sign; "

inherit image
