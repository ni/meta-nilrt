SUMMARY = "Install the previous NI OS for migration"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

IMAGE_FSTYPES = "tar.bz2"

IMAGE_INSTALL = "\
        grub-efi-nilrt \
"

inherit image

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
SRC_URI = "file://grub_migrate.cfg"

DEPENDS = "grub-efi grub-bootconf gzip-native cpio-native"

do_rootfs[depends] = " \
    safemode-restore-image:do_image_complete \
"

do_boot_image() {
    # Extract kernel from safemode-restore-image and put it in EFI partition
    (cd ${WORKDIR} && gunzip --stdout ${DEPLOY_DIR_IMAGE}/safemode-restore-image-${MACHINE}.cpio.gz |cpio -di "boot/bzImage*")

    install -m 0755 ${WORKDIR}/boot/$(readlink "${WORKDIR}/boot/bzImage") ${IMAGE_ROOTFS}/bzImage
    install -m 0755 ${DEPLOY_DIR_IMAGE}/safemode-restore-image-${MACHINE}.cpio.gz ${IMAGE_ROOTFS}/initrd

    # Move efi folder to root
    mv "${IMAGE_ROOTFS}/boot/efi" "${IMAGE_ROOTFS}/"

    # Install grub_migrate.cfg instead of default grub.cfg
    install -m 0644 ${THISDIR}/files/grub_migrate.cfg ${IMAGE_ROOTFS}/efi/nilrt/grub.cfg

    # image.bbclass installs several files/folders which are not needed on
    # an EFI system partition. Cleanup all non-boot related files.
    find "${IMAGE_ROOTFS}" -mindepth 1 \
            -not -path "${IMAGE_ROOTFS}/bzImage" \
            -not -path "${IMAGE_ROOTFS}/initrd" \
            -a -not -path "${IMAGE_ROOTFS}/efi" \
            -a -not -path "${IMAGE_ROOTFS}/efi/nilrt" \
            -a -not -path "${IMAGE_ROOTFS}/efi/nilrt/*" \
            -delete
}

IMAGE_PREPROCESS_COMMAND += "do_boot_image; "

