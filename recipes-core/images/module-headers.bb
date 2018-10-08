require squashfs-image.inc

DESCRIPTION = "SquashFS image containing headers & objects used to version kernel modules."

IMAGE_INSTALL = "${PREFERRED_PROVIDER_virtual/kernel}-module-versioning-headers"

IMAGE_BASENAME = "${PREFERRED_PROVIDER_virtual/kernel}-module-headers"

do_cleanup_append() {
	rm -rf ${IMAGE_ROOTFS}/{run,usr,lib}
}
