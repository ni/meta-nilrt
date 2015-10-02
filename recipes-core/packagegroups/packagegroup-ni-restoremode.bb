SUMMARY = "initramfs specific packages for NI Linux Realtime distribution"
LICENSE = "MIT"
PR = "r1"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} += "\
    base-passwd         \
    bash                \
    busybox             \
    bzip2               \
    dmidecode           \
    dosfstools          \
    e2fsprogs-mke2fs    \
    e2fsprogs-tune2fs   \
    efibootmgr          \
    efivar              \
    fw-printenv         \
    gptfdisk-sgdisk     \
    grub                \
    grub-editenv        \
    init-restore-mode   \
    kernel-module-atkbd \
    parted              \
    tar                 \
    util-linux          \
    "
