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

LICENSE_TAR = "${DEPLOY_DIR}/licenses.tar.gz"

addtask tar_licenses after do_populate_lic before do_rootfs

do_tar_licenses() {
	rm -f ${LICENSE_TAR}
	tar cfz ${LICENSE_TAR} -C ${DEPLOY_DIR} licenses
}

ROOTFS_POSTPROCESS_COMMAND += "do_copy_licenses_tar; "

do_copy_licenses_tar() {
	cp ${LICENSE_TAR} ${IMAGE_ROOTFS}
}

