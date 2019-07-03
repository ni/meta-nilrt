SUMMARY = "Install the next OS for migration"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI_x64 = "file://bootimage.cfg"

DEPENDS_append_x64 += "grub-efi"
do_install[depends] = "restore-mode-image:do_image_complete ${PREFERRED_PROVIDER_virtual/kernel}:do_deploy"

S = "${WORKDIR}"

do_install_append_x64() {
    install -d ${D}/boot/.newNILinuxRT/EFI/BOOT
    install -m 0755 ${S}/bootimage.cfg  ${D}/boot/.newNILinuxRT/
    install -m 0755 ${DEPLOY_DIR_IMAGE}/grub-efi-bootx64.efi ${D}/boot/.newNILinuxRT/EFI/BOOT/bootx64.efi
    install -m 0755 ${DEPLOY_DIR_IMAGE}/bzImage ${D}/boot/.newNILinuxRT/
    install -m 0755 ${DEPLOY_DIR_IMAGE}/restore-mode-image-x64.cpio.gz ${D}/boot/.newNILinuxRT/initrd
}

do_install_append_xilinx-zynqhf() {
    #this hack is only to create the ipk, otherwise if only hidden folders are installed, the ipk won't be created
    install -d ${D}/boot
    install -d ${D}/.newNILinuxRT/.safe
    install -d ${D}/.newNILinuxRT/.restore/dtbs
    install -m 0644 ${PKG_CONFIG_SYSROOT_DIR}/boot/forwards_migrate.itb ${D}/.newNILinuxRT/
    install -m 0644 ${PKG_CONFIG_SYSROOT_DIR}/boot/nxg_redirect_safemode_boot.itb ${D}/.newNILinuxRT/.safe/
    for f in ${DEPLOY_DIR_IMAGE}/uImage-ni-*.dtb; do
        dtb_name=`echo $f | awk -F"[-.]" '{print $(NF-1)}'`
        install -m 0644 $f ${D}/.newNILinuxRT/.restore/dtbs/ni-0x$dtb_name.dtb
    done
    install -m 0644 ${PKG_CONFIG_SYSROOT_DIR}/boot/restore.scr ${D}/.newNILinuxRT/.restore/
    install -m 0644 ${DEPLOY_DIR_IMAGE}/restore-mode-image-xilinx-zynqhf.cpio.gz.u-boot ${D}/.newNILinuxRT/.restore/ramdisk
    install -m 0644 ${DEPLOY_DIR_IMAGE}/uImage ${D}/.newNILinuxRT/.restore/
}

FILES_${PN}_x64 = "\
    /boot/.newNILinuxRT/* \
"
FILES_${PN}_xilinx-zynqhf = "\
    /boot \
    /.newNILinuxRT/* \
    /.newNILinuxRT/.restore/* \
    /.newNILinuxRT/.restore/dtbs/* \
    /.newNILinuxRT/.safe/* \
"
