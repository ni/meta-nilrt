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
    init-restore-mode   \
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
    eudev               \
    kernel-module-atkbd \
    "

RDEPENDS_${PN}_append_xilinx-zynqhf = "\
    mtd-utils           \
    mtd-utils-ubifs     \
    u-boot-fw-utils     \
    "

DEPENDS += "linux-nilrt"

# important: don't change this to RDEPENDS, we don't want to include safemode-image
# in all restore-mode packages, we just want to ensure it gets built
DEPENDS_x64 += "safemode-image"
