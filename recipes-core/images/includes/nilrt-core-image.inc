DESCRIPTION ?= "Base image setup used in all NILRT images"
LICENSE = "MIT"

IMAGE_FSTYPES = "tar.bz2"

IMAGE_FEATURES ??= ""
IMAGE_LINGUAS ??= ""

IMAGE_PREPROCESS_COMMAND += "rootfs_update_timestamp;"

# NILRT images expect the default kernel is an actual file
# (e.g. bzImage) and not a symbolic link; fix it up here.
ROOTFS_POSTPROCESS_COMMAND += "move_kernel;"
move_kernel() {
	for type in ${KERNEL_IMAGETYPES} ; do
		if [ -L "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/${type}" ]; then
			mv "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/$(readlink "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/${type}")" \
			   "${IMAGE_ROOTFS}/${KERNEL_IMAGEDEST}/${type}"
		fi
	done
}

inherit core-image
