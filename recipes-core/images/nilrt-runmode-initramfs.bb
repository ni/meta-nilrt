DESCRIPTION = "A small initramfs for booting NILRT targets into runmode."


# ==============================================================================
# RECIPE VARIABLES
# ==============================================================================

PV = "${DISTRO_VERSION}"

require includes/nilrt-core-image.inc


# ==============================================================================
# SOFTWARE DISTRIBUTION
# ==============================================================================

# Do not pollute the initrd image with rootfs features.
IMAGE_FEATURES = ""

IMAGE_LINGUAS = ""

IMAGE_INSTALL = "\
	${ROOTFS_BOOTSTRAP_INSTALL} \
	packagegroup-ni-initramfs \
	init-nilrt-runmode-initramfs \
"

BAD_RECOMMENDATIONS += "\
	shared-mime-info \
	ca-certificates \
"

NO_RECOMMENDATIONS = "1"

PACKAGE_EXCLUDE += "python-core python3-core"

# 1024 MB = 1/2 the smallest x64 cRIO memory size
INITRAMFS_MAXSIZE = "1048578"


# ==============================================================================
# TASKS
# ==============================================================================

bootimg_fixup () {
	# Empty out /boot. The kernel and grub are added to the exterior
	# image and not this ramdisk container.
	rm -rf "${IMAGE_ROOTFS}/boot"
}

IMAGE_PREPROCESS_COMMAND += " bootimg_fixup; "


# ==============================================================================
# DEPLOYMENT
# ==============================================================================

INITRAMFS_FSTYPES ?= "cpio.xz"
IMAGE_NAME_SUFFIX = ""
XZ_COMPRESSION_LEVEL = "-e -9"
XZ_INTEGRITY_CHECK = "crc32"

# Some BSPs use IMAGE_FSTYPES:<machine override> which would override
# a plain assignment to IMAGE_FSTYPES, so derive it from INITRAMFS_FSTYPES
# via setVar to bypass machine-level overrides on IMAGE_FSTYPES.
python () {
    d.setVar("IMAGE_FSTYPES", d.getVar("INITRAMFS_FSTYPES"))
}
