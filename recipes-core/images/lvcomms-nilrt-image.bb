# Start with the minial image as a base
require minimal-nilrt-image.bb

# Change description
DESCRIPTION = " \
    Fully-featured run-mode image for LabVIEW Comms. Includes drivers, \
    daemons, and other utilities required for RT targets to interact \
    with Comms. \
"

# Append additional software
IMAGE_INSTALL_NODEPS += " \
    kernel-modules \
    linux-firmware \
    lvrt \
    niminionagent \
    ni-pxiplatformservices \
    ni-flexrio \
    ni-usrp-rio \
    ni-sync \
    mt \
"

COPY_LIC_MANIFEST = "1"
COPY_LIC_DIRS = "1"
LICENSE_TAR = "${IMAGE_ROOTFS}/usr/share/common-licenses.bz2"

tar_licenses() {
	rm -f ${LICENSE_TAR}
	tar cf ${LICENSE_TAR} -C ${IMAGE_ROOTFS}/usr/share -I pbzip2 common-licenses
	rm -rf ${IMAGE_ROOTFS}/usr/share/common-licenses
	[ ! -e "${IMAGE_ROOTFS}/usr/share/common-licenses" ]
}

ROOTFS_POSTPROCESS_COMMAND += "tar_licenses; "

