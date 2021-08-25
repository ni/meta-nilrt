DESCRIPTION = "NI LinuxRT Safemode RAM disk for x64 Targets"

require nilrt-proprietary.inc
require include/licenses.inc

# TODO: Right now I'm setting PV to be the same as the distro version
#       as that's the closest to the os-common versioning, but it loses
#       the "incrementing build number" aspect from os-common.
PV = "${DISTRO_VERSION}"
SAFEMODE_VERSION = "${PV}-${BUILDNAME}"

# useradd and groupadd need to be on sysroot
do_rootfs[depends] += "shadow-native:do_populate_sysroot"

# kernel recipe requires depmodwrapper to populate modules.dep
do_rootfs[depends] += "depmodwrapper-cross:do_populate_sysroot"

IMAGE_FEATURES += "\
	package-management \
"

IMAGE_INSTALL += "\
	packagegroup-ni-base \
	packagegroup-ni-tzdata \
	packagegroup-ni-wifi \
	packagegroup-ni-safemode \
"

IMAGE_INSTALL_NODEPS += "\
	${NI_PROPRIETARY_BASE_PACKAGES} \
"

bootimg_fixup () {
	# TODO: os-common does this but is this necessary?
	cp "${IMAGE_ROOTFS}/sbin/init.sysvinit" "${IMAGE_ROOTFS}/init"

	# Empty out /boot. The kernel and grub are added to the exterior
	# image and not this ramdisk container.
	rm -rf "${IMAGE_ROOTFS}/boot"

	# Set up mount points.
	mkdir -p "${IMAGE_ROOTFS}/boot"
	mkdir -p "${IMAGE_ROOTFS}/etc/natinst/share"
	mkdir -p "${IMAGE_ROOTFS}/mnt/userfs"

	echo "LABEL=nibootfs /boot ext4 sync 0 0" >> "${IMAGE_ROOTFS}/etc/fstab"
	echo "LABEL=niconfig /etc/natinst/share ext4 sync 0 0" >> "${IMAGE_ROOTFS}/etc/fstab"
	echo "LABEL=nirootfs /mnt/userfs ext4 sync 0 0" >> "${IMAGE_ROOTFS}/etc/fstab"

	# Add safemode marker
	echo ${SAFEMODE_VERSION} > "${IMAGE_ROOTFS}/etc/natinst/safemode"

	# opkg cleanup
	opkg -o ${IMAGE_ROOTFS} -f ${IPKGCONF_TARGET} clean
}

IMAGE_PREPROCESS_COMMAND += " bootimg_fixup; "

addtask image_build_test before do_rootfs

# We always want package-management support in this image, fail if not enabled
do_image_build_test() {
	if ! echo ${IMAGE_FEATURES} | grep -q "package-management"; then
		exit 1
	fi
}

require nilrt-image-common.inc

IMAGE_FSTYPES = "cpio.xz"
IMAGE_NAME_SUFFIX = ""
XZ_COMPRESSION_LEVEL = "-e -9"
XZ_INTEGRITY_CHECK = "crc32"
