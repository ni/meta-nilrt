SUMMARY = "Install the next OS for migration"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI_x64 = "file://bootimage.cfg"

DEPENDS_append_x64 += "grub-efi"
do_install[depends] = "restore-mode-image:do_image_complete ${PREFERRED_PROVIDER_virtual/kernel}:do_deploy"

S = "${WORKDIR}"

do_install_append_x64() {
    install -d ${D}/newNILinuxRT/EFI/BOOT
    install -m 0755 ${S}/bootimage.cfg  ${D}/newNILinuxRT/
    install -m 0755 ${DEPLOY_DIR_IMAGE}/grub-efi-bootx64.efi ${D}/newNILinuxRT/EFI/BOOT/bootx64.efi
    install -m 0755 ${DEPLOY_DIR_IMAGE}/bzImage ${D}/newNILinuxRT/
    install -m 0755 ${DEPLOY_DIR_IMAGE}/restore-mode-image-x64.cpio.gz ${D}/newNILinuxRT/initrd
}

FILES_${PN}_x64 = "\
    /newNILinuxRT/* \
"
