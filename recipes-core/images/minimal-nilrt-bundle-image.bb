DESCRIPTION = "Filesystem image/archive of NILRT boot partition containing boot loader and minimal NILRT image"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

IMAGE_FSTYPES = "tar.bz2"

DEPENDS = "grub-efi grub-bootconf ${PREFERRED_PROVIDER_virtual/kernel}"

IMAGE_INSTALL = " \
	grub-efi \
	grub-bootconf \
	kernel-image-bzimage \
"

INITRAMFS_IMAGE = "nilrt-initramfs"
do_rootfs[depends] += "${INITRAMFS_IMAGE}:do_image_complete"

BASEROOTFS_IMAGE = "minimal-nilrt-image"
do_rootfs[depends] += "${BASEROOTFS_IMAGE}:do_image_complete"

bootimg_fixup() {
	# Install factory image to /boot/EFI/BOOT/
	install -m 0644 "${DEPLOY_DIR_IMAGE}/${BASEROOTFS_IMAGE}-${MACHINE}.squashfs" "${IMAGE_ROOTFS}/boot/baserootfs.squashfs"

	# Install initramfs image to /boot/EFI/BOOT/
	install -m 0644 "${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE}-${MACHINE}.cpio.gz" "${IMAGE_ROOTFS}/boot/initrd.cpio.gz"

	# Install version file
	echo "${BUILDNAME}" >"${IMAGE_ROOTFS}/boot/version"

	# Generate readme.txt file to describe image contents
	echo  >"${IMAGE_ROOTFS}/boot/readme.txt" "${PN} ${PV} ${PR} system partition image:"
	echo >>"${IMAGE_ROOTFS}/boot/readme.txt" " - bootx64.efi: Grub EFI binary"
	echo >>"${IMAGE_ROOTFS}/boot/readme.txt" " - grub.cfg: Grub configuration"
	echo >>"${IMAGE_ROOTFS}/boot/readme.txt" " - bzImage: $(readlink "${IMAGE_ROOTFS}/boot/bzImage") kernel image"
	echo >>"${IMAGE_ROOTFS}/boot/readme.txt" " - initrd.cpio.gz: ${INITRAMFS_IMAGE}-${MACHINE}.cpio.gz ramdisk image"
	echo >>"${IMAGE_ROOTFS}/boot/readme.txt" " - baserootfs.squashfs: ${BASEROOTFS_IMAGE}-${MACHINE}.tar.bz2 root file system image"

	# Install bzImage-<version> to /boot/bzImage
	install -m 0644 "${IMAGE_ROOTFS}/boot/$(readlink "${IMAGE_ROOTFS}/boot/bzImage")" "${IMAGE_ROOTFS}/boot/bzImage"
	# Remove kernel-<version> file
	rm ${IMAGE_ROOTFS}/boot/bzImage-*

	# Bitbake insists on installing glibc which is not needed on
	#  EFI system partition. Cleanup all non-boot related files.
	find "${IMAGE_ROOTFS}" -mindepth 1 \
		-not -path "${IMAGE_ROOTFS}/boot" \
		-a -not -path "${IMAGE_ROOTFS}/boot/*" \
		-a -not -path "${IMAGE_ROOTFS}/boot/EFI" \
		-a -not -path "${IMAGE_ROOTFS}/boot/EFI/BOOT" \
		-a -not -path "${IMAGE_ROOTFS}/boot/EFI/BOOT/*" \
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

IMAGE_PREPROCESS_COMMAND += " bootimg_fixup; "

inherit image
