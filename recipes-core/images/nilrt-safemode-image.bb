DESCRIPTION = "NI LinuxRT Safemode Image for x64 Targets"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

IMAGE_FSTYPES = "tar.gz"
IMAGE_NAME_SUFFIX = ""

DEPENDS += "${PREFERRED_PROVIDER_virtual/kernel}"

PV = "${DISTRO_VERSION}"

SRC_URI += "\
	file://bootimage.ini \
	file://${BPN}.preinst \
"

IMAGE_INSTALL += "\
	kernel-image-bzimage \
	fw-printenv \
	nilrt-grub-safemode \
"

RAMDISK_IMAGE = "nilrt-safemode-ramdisk"
do_rootfs[depends] += "${RAMDISK_IMAGE}:do_image_complete"

CUSTOM_KERNEL_PATH ?= "/boot"

bootimg_fixup() {
	install -m 0644 "${DEPLOY_DIR_IMAGE}/${RAMDISK_IMAGE}-${MACHINE}.cpio.xz" "${IMAGE_ROOTFS}/boot/ramdisk.xz"

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
	mv "$(realpath ${IMAGE_ROOTFS}/${CUSTOM_KERNEL_PATH}/bzImage)" "${IMAGE_ROOTFS}/${CUSTOM_KERNEL_PATH}/bzImage.real"
	rm -f "${IMAGE_ROOTFS}/boot/bzImage"
	mv "${IMAGE_ROOTFS}/${CUSTOM_KERNEL_PATH}/bzImage.real" "${IMAGE_ROOTFS}/boot/bzImage"

	SAFEMODE_VERSION=$(xz -cd "${IMAGE_ROOTFS}/boot/ramdisk.xz" | cpio --to-stdout --quiet -i "etc/natinst/safemode")
	install -m 0644 "${THISDIR}/files/bootimage.ini" "${IMAGE_ROOTFS}/boot/bootimage.ini"
	sed -i "s/%component_version%/$SAFEMODE_VERSION/" "${IMAGE_ROOTFS}/boot/bootimage.ini"

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

IMAGE_PREPROCESS_COMMAND += " bootimg_fixup; ensure_expected_files; "

inherit image
