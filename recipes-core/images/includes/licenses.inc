
COPY_LIC_MANIFEST = "1"
COPY_LIC_DIRS = "1"
LICENSE_TAR = "${IMAGE_ROOTFS}/usr/share/common-licenses.tar.bz2"

tar_licenses() {
	rm -f ${LICENSE_TAR}
	tar cf ${LICENSE_TAR} -C ${IMAGE_ROOTFS}/usr/share --bzip2 common-licenses
	rm -rf ${IMAGE_ROOTFS}/usr/share/common-licenses
	[ ! -e "${IMAGE_ROOTFS}/usr/share/common-licenses" ]
}

ROOTFS_POSTPROCESS_COMMAND += "tar_licenses; "

