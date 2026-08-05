DESCRIPTION = "NI Linux RT safemode rootfs archive"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

IMAGE_FSTYPES = "tar.gz"
IMAGE_NAME_SUFFIX = ""

DEPENDS += "${PREFERRED_PROVIDER_virtual/kernel}"

# UEFI_SB is only defined for recipes inheriting user-key-store, which this is not.
SECURE_BOOT_ENABLED = "${@bb.utils.contains('DISTRO_FEATURES', 'efi-secure-boot', '1', '0', d)}"

PV = "${DISTRO_VERSION}"

SRC_URI += "\
	file://bootimage.ini \
	file://${BPN}.preinst \
"

IMAGE_INSTALL = "\
	fw-printenv \
"

IMAGE_INSTALL:append:x64 = "\
	kernel-image-bzimage \
	nilrt-grub-safemode \
	${@bb.utils.contains('DISTRO_FEATURES', 'efi-secure-boot', 'packagegroup-efi-secure-boot', '', d)} \
"

RAMDISK_IMAGE = "nilrt-safemode-initramfs"
do_rootfs[depends] += "${RAMDISK_IMAGE}:do_image_complete"
do_rootfs[depends] += "${@' ${RAMDISK_IMAGE}:do_sign_secure_boot' if d.getVar('SECURE_BOOT_ENABLED') == '1' else ''}"

bootimg_fixup() {
	install -d "${IMAGE_ROOTFS}/boot"

	install -m 0644 "${DEPLOY_DIR_IMAGE}/${RAMDISK_IMAGE}-${MACHINE}.cpio.xz" "${IMAGE_ROOTFS}/boot/ramdisk.xz"

	if [ "${SECURE_BOOT_ENABLED}" = "1" ] && [ -n "${SB_FILE_EXT}" ]; then
		if [ -f "${DEPLOY_DIR_IMAGE}/${RAMDISK_IMAGE}-${MACHINE}.cpio.xz${SB_FILE_EXT}" ]; then
			install -m 0644 "${DEPLOY_DIR_IMAGE}/${RAMDISK_IMAGE}-${MACHINE}.cpio.xz${SB_FILE_EXT}" "${IMAGE_ROOTFS}/boot/ramdisk.xz${SB_FILE_EXT}"
		fi
	fi

	install -m 0755 "${THISDIR}/files/${BPN}.preinst" "${IMAGE_ROOTFS}/boot/preinst"

	# Promote EFI_NI_vars and SMBIOS_NI_vars to /boot
	install -m 0644 "${IMAGE_ROOTFS}/${datadir}/fw_printenv/EFI_NI_vars" "${IMAGE_ROOTFS}/boot/EFI_NI_vars"
	install -m 0644 "${IMAGE_ROOTFS}/${datadir}/fw_printenv/SMBIOS_NI_vars" "${IMAGE_ROOTFS}/boot/SMBIOS_NI_vars"

	# Old grub, niinstallsafemode, and nivalidatestartup scripts expect
	# to find a "ramdisk.gz" file. Create an empty file for backwards
	# compatibility.
	printf '\0' >"${IMAGE_ROOTFS}/boot/ramdisk.gz"

	# The kernel was installed with a symbolic link from 'bzImage'
	# to the actual versioned file. Remove the redirection so that
	# we just have a 'bzImage'
	mv "$(realpath ${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/bzImage)" "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/bzImage.real"
	rm -f "${IMAGE_ROOTFS}/boot/bzImage"
	mv "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/bzImage.real" "${IMAGE_ROOTFS}/boot/bzImage"
	if [ "${SECURE_BOOT_ENABLED}" = "1" ] && [ -n "${SB_FILE_EXT}" ]; then
		if [ -e "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/bzImage${SB_FILE_EXT}" ]; then
			kernel_sig_src="${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/bzImage${SB_FILE_EXT}"
			if [ -L "${kernel_sig_src}" ]; then
				# Preserve signature contents rather than a potentially dangling symlink.
				install -m 0644 "$(realpath "${kernel_sig_src}")" "${IMAGE_ROOTFS}/boot/bzImage${SB_FILE_EXT}"
			else
				mv "${kernel_sig_src}" "${IMAGE_ROOTFS}/boot/bzImage${SB_FILE_EXT}"
			fi
		else
			# Some package managers may not preserve the stable signature symlink.
			# Fall back to a versioned kernel sidecar and normalize its final name.
			versioned_sig="$(ls -1 "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/bzImage-"*"${SB_FILE_EXT}" 2>/dev/null | sort | tail -n 1)"
			if [ -n "${versioned_sig}" ] && [ -e "${versioned_sig}" ]; then
				install -m 0644 "${versioned_sig}" "${IMAGE_ROOTFS}/boot/bzImage${SB_FILE_EXT}"
			fi
		fi
	fi
	rm -rf "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}"

	install -m 0644 "${THISDIR}/files/bootimage.ini" "${IMAGE_ROOTFS}/boot/bootimage.ini"
	sed -i "s/%component_version%/${BUILDNAME}/" "${IMAGE_ROOTFS}/boot/bootimage.ini"

	# We've assembled everything we want under /boot.
	# We now want to get rid of everything else.
	find "${IMAGE_ROOTFS}" -mindepth 1 \
		-not -path "${IMAGE_ROOTFS}/boot" \
		-a -not -path "${IMAGE_ROOTFS}/boot/*" \
		-delete

	# Promote the boot directory to the top level.
	mv "${IMAGE_ROOTFS}/boot"/* "${IMAGE_ROOTFS}/"
	rmdir "${IMAGE_ROOTFS}/boot"
}

# Sanity check that the image contains all the files that it should.
EXPECTED_CONTENTS = "\
	bootimage.cfg \
	bootimage.ini \
	bzImage \
	ramdisk.gz \
	ramdisk.xz \
	grub.cfg \
	grubenv \
	EFI_NI_vars \
	SMBIOS_NI_vars \
"

ensure_expected_files() {
	for f in "${EXPECTED_FILES}"; do
		if [ ! -e "${IMAGE_ROOTFS}/${f}" ]; then
			echo "ERROR: ${f} is required in safemode image." 1>&2
			exit 1
		fi
	done
}

ensure_secure_boot_files() {
	if [ "${SECURE_BOOT_ENABLED}" != "1" ] || [ -z "${SB_FILE_EXT}" ]; then
		return
	fi

	for f in "bzImage${SB_FILE_EXT}" "ramdisk.xz${SB_FILE_EXT}"; do
		if [ -e "${IMAGE_ROOTFS}/boot/${f}" ] || [ -e "${IMAGE_ROOTFS}/${f}" ]; then
			continue
		fi

		bbfatal "${f} is required in secure-boot safemode image. Checked ${IMAGE_ROOTFS}/boot/${f} and ${IMAGE_ROOTFS}/${f}."
	done
}

IMAGE_PREPROCESS_COMMAND += " bootimg_fixup; ensure_expected_files; ensure_secure_boot_files; "

inherit image
