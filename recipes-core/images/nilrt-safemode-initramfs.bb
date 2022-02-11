DESCRIPTION = "NI Linux RT safemode initramfs"

require includes/nilrt-image-base.inc

# It is inappropriate for an initramfs to install external opkg feed confs.
ROOTFS_POSTPROCESS_COMMAND_remove = "install_additional_feeds;"


require includes/nilrt-proprietary.inc


PV = "${DISTRO_VERSION}"


IMAGE_INSTALL += "\
	packagegroup-ni-safemode \
	packagegroup-ni-wifi \
"

IMAGE_INSTALL_NODEPS += "\
	${NI_PROPRIETARY_COMMON_PACKAGES} \
	${NI_PROPRIETARY_SAFEMODE_PACKAGES} \
"

BAD_RECOMMENDATIONS_append_pn-${PN} += "shared-mime-info"

# Do not allow python to be installed into safemode ramdisk due to size
PACKAGE_EXCLUDE += "python-core python3-core"

PACKAGE_EXCLUDE += "rauc-mark-good"


# Radeon firmware is huge and is not included in the safemode.
# Blacklist the kernel module that gets automatically included.
remove_radeon () {
	echo "blacklist radeon" > "${IMAGE_ROOTFS}/etc/modprobe.d/blacklist_radeon.conf"
}

bootimg_fixup () {
	# Empty out /boot. The kernel and grub are added to the exterior
	# image and not this ramdisk container.
	rm -rf "${IMAGE_ROOTFS}/boot"

	# Set up mount points.
	mkdir -p "${IMAGE_ROOTFS}/boot"
	mkdir -p "${IMAGE_ROOTFS}/etc/natinst/share"
	mkdir -p "${IMAGE_ROOTFS}/mnt/userfs"

	echo "LABEL=nibootfs /boot ext4 sync 0 0" >> "${IMAGE_ROOTFS}/etc/fstab"
	echo "LABEL=niconfig /etc/natinst/share ext4 sync 0 0" >> "${IMAGE_ROOTFS}/etc/fstab"
	echo "LABEL=nirootfs /mnt/userfs ext4 defaults 0 0" >> "${IMAGE_ROOTFS}/etc/fstab"

	# Add safemode marker
	echo "safemode" > "${IMAGE_ROOTFS}/etc/natinst/safemode"

	# Override NISystemWebServer config with one for safemode
	rm -f ${IMAGE_ROOTFS}/etc/natinst/appweb/NISystemWebServer.conf
	install -m 0644 "${THISDIR}/files/NISystemWebServer.conf.safemode" \
		"${IMAGE_ROOTFS}/etc/natinst/appweb/NISystemWebServer.conf"

	# opkg cleanup
	opkg -o ${IMAGE_ROOTFS} -f ${IPKGCONF_TARGET} clean
}

IMAGE_PREPROCESS_COMMAND += " remove_radeon; bootimg_fixup; "


# We always want package-management support in this image, fail if not enabled
do_image_build_test() {
	if ! echo ${IMAGE_FEATURES} | grep -q "package-management"; then
		exit 1
	fi
}

addtask image_build_test before do_rootfs


IMAGE_FSTYPES = "cpio.xz"
IMAGE_NAME_SUFFIX = ""
XZ_COMPRESSION_LEVEL = "-e -9"
XZ_INTEGRITY_CHECK = "crc32"
