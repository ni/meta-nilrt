DESCRIPTION = "SquashFS image containing the toolchain used to version kernel modules."

IMAGE_INSTALL = "binutils binutils-symlinks libgcc-dev gcc gcc-symlinks make"

# disable image features we don't need
IMAGE_FEATURES = ""
IMAGE_LINGUAS = ""
FEED_URIS = ""

# this option avoids creating a /dev folder in the image
USE_DEVFS = "1"

IMAGE_FSTYPES = "squashfs"

# clean-up files we don't need
do_cleanup() {
	echo ${TARGET_PREFIX} >> /tmp/img.log

	rm -rf ${IMAGE_ROOTFS}/etc
	rm -rf ${IMAGE_ROOTFS}/sbin
	rm -rf ${IMAGE_ROOTFS}/usr/lib/eglibc
	rm -rf ${IMAGE_ROOTFS}/var

	TARGET_PATH_PREFIX=$(echo ${TARGET_PREFIX} | sed -e 's#-$##')
	rm -f ${IMAGE_ROOTFS}/usr/lib/gcc/gcc/${TARGET_PATH_PREFIX}/?.*.*/cc1plus
}

IMAGE_PREPROCESS_COMMAND += "do_cleanup; "

inherit image