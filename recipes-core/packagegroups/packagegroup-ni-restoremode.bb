SUMMARY = "initramfs specific packages for NI Linux Realtime distribution"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} += "\
    base-passwd         \
    bash                \
    busybox             \
    bzip2               \
    dosfstools          \
    e2fsprogs-mke2fs    \
    e2fsprogs-tune2fs   \
    gptfdisk-sgdisk     \
    parted              \
    tar                 \
    util-linux          \
    "

RDEPENDS_${PN}_append_x64 = "\
    dmidecode           \
    efibootmgr          \
    efivar              \
    fw-printenv         \
    grub                \
    grub-editenv        \
    init-restore-mode   \
    kernel-module-atkbd \
    "

RDEPENDS_${PN}_append_xilinx-zynqhf = "\
    mtd-utils           \
    mtd-utils-ubifs     \
    u-boot-fw-utils		\
    "
