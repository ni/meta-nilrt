require squashfs-image.inc

DESCRIPTION = "SquashFS image containing the toolchain used to version kernel modules."

IMAGE_INSTALL = "binutils binutils-symlinks libgcc-dev gcc gcc-symlinks make util-linux-libmount"

IMAGE_BASENAME = "${PREFERRED_PROVIDER_virtual/kernel}-module-tools"

do_cleanup_append() {
	TARGET_PATH_PREFIX=$(echo ${TARGET_PREFIX} | sed -e 's#-$##')
	rm -f ${IMAGE_ROOTFS}/usr/libexec/gcc/${TARGET_PATH_PREFIX}/?.*.*/cc1plus
	rm -f ${IMAGE_ROOTFS}/usr/bin/ld.gold
	rm -f ${IMAGE_ROOTFS}/usr/bin/${TARGET_PREFIX}ld.gold
	rm -f ${IMAGE_ROOTFS}/usr/bin/dwp
	rm -f ${IMAGE_ROOTFS}/usr/bin/${TARGET_PREFIX}dwp
}
