SUMMARY = "Install the next OS for migration"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI_x64 = "file://bootimage.cfg"

DEPENDS = " restore-mode-image "
DEPENDS_append_x64 = " grub-efi "
do_install[depends] = " restore-mode-image:do_rootfs "

PR = "r1"

S = "${WORKDIR}"

do_install_append_x64() {
    install -d ${D}/boot/.newNILinuxRT/EFI/BOOT
    install -m 0755 ${S}/bootimage.cfg  ${D}/boot/.newNILinuxRT/
    install -m 0755 ${DEPLOY_DIR_IMAGE}/bootx64.efi ${D}/boot/.newNILinuxRT/EFI/BOOT/
    install -m 0755 ${DEPLOY_DIR_IMAGE}/bzImage ${D}/boot/.newNILinuxRT/
    install -m 0755 ${DEPLOY_DIR_IMAGE}/restore-mode-image-x64.cpio.gz ${D}/boot/.newNILinuxRT/initrd
}

do_install_append_xilinx-zynqhf() {
    #this hack is only to create the ipk, otherwise if only hidden folders are installed, the ipk won't be created
    install -d ${D}/boot
    install -d ${D}/.newNILinuxRT/.safe
    install -d ${D}/.newNILinuxRT/.restore/dtbs
    install -m 0644 ${PKG_CONFIG_SYSROOT_DIR}/boot/linux_fw_migrate.itb ${D}/.newNILinuxRT/
    install -m 0644 ${PKG_CONFIG_SYSROOT_DIR}/boot/linux_next_safemode.itb ${D}/.newNILinuxRT/.safe/
    for f in ${DEPLOY_DIR_IMAGE}/uImage-ni-*.dtb; do
        dtb_name=`echo $f | awk -F"[-.]" '{print $(NF-1)}'`
        install -m 0644 $f ${D}/.newNILinuxRT/.restore/dtbs/ni-0x$dtb_name.dtb
    done
    install -m 0644 ${PKG_CONFIG_SYSROOT_DIR}/boot/restore.scr ${D}/.newNILinuxRT/.restore/
    install -m 0644 ${DEPLOY_DIR_IMAGE}/restore-mode-image-xilinx-zynqhf.cpio.gz.u-boot ${D}/.newNILinuxRT/.restore/ramdisk
    install -m 0644 ${DEPLOY_DIR_IMAGE}/uImage ${D}/.newNILinuxRT/.restore/
}

FILES_${PN} = ""

FILES_${PN}_append_x64 = "/boot/.newNILinuxRT/* \
                         "
FILES_${PN}_append_xilinx-zynqhf = "/boot   \
                                    /.newNILinuxRT/*    \
                                    /.newNILinuxRT/.restore/*   \
                                    /.newNILinuxRT/.restore/dtbs/*  \
                                    /.newNILinuxRT/.safe/*  \
                                   "
