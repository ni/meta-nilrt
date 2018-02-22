
# Note that this include only works when LICENSE_CREATE_PACKAGE is set to "1"
# globally (like in local.conf), as it relies on all packages creating
# corresponding license packages

LICENSE_TAR = "${IMAGE_ROOTFS}/usr/share/common-licenses.tar.xz"

tar_license_pkgs() {
	rm -f ${LICENSE_TAR}
	tar cfJ ${LICENSE_TAR} -C ${IMAGE_ROOTFS}/usr/share licenses
	rm -rf ${IMAGE_ROOTFS}/usr/share/licenses
	[ ! -e "${IMAGE_ROOTFS}/usr/share/licenses" ]
}

ROOTFS_POSTPROCESS_COMMAND += "tar_license_pkgs; "

