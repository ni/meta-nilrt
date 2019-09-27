SUMMARY = "Repartition a safemode based installation to an A/B EFI implementation"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "\
    file://bootimage.cfg \
    file://${PN}-install \
"

RDEPENDS_${PN} += "bash"
DEPENDS_append_x64 += "grub-efi"
do_install[depends] = "restore-mode-image:do_image_complete ${PREFERRED_PROVIDER_virtual/kernel}:do_deploy"

do_install_append_x64() {
    install -d ${D}/sbin
    install -d ${D}/newNILinuxRT/EFI/BOOT
    install -m 0755 ${WORKDIR}/bootimage.cfg  ${D}/newNILinuxRT/
    install -m 0755 ${WORKDIR}/${PN}-install  ${D}/sbin/nilrt-install
    install -m 0755 ${DEPLOY_DIR_IMAGE}/grub-efi-bootx64.efi ${D}/newNILinuxRT/EFI/BOOT/bootx64.efi
    install -m 0755 ${DEPLOY_DIR_IMAGE}/bzImage ${D}/newNILinuxRT/
    install -m 0755 ${DEPLOY_DIR_IMAGE}/restore-mode-image-x64.cpio.gz ${D}/newNILinuxRT/initrd
}

pkg_postinst_${PN}() {
    # If installed in safemode, move the payload to a non-volatile location
    if [ -d /mnt/userfs ]; then
        rm -fr /mnt/userfs/newNILinuxRT
        cp -r /newNILinuxRT /mnt/userfs

        mkdir -p /mnt/userfs/sbin
        cp /sbin/nilrt-install /mnt/userfs/sbin
    fi
}

pkg_prerm_${PN}() {
    # If the package is removed in safemode, perform proper cleanup
    rm -fr /mnt/userfs/newNILinuxRT
    rm -fr /mnt/userfs/sbin/nilrt-install
}

FILES_${PN}_x64 = "\
    /newNILinuxRT/* \
    /sbin/nilrt-install \
"
