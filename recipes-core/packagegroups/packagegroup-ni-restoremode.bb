SUMMARY = "initramfs specific packages for NI Linux Realtime distribution"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} += "\
    base-passwd         \
    bash                \
    bzip2               \
    coreutils           \
    dosfstools          \
    e2fsprogs           \
    e2fsprogs-mke2fs    \
    e2fsprogs-tune2fs   \
    findutils           \
    gawk                \
    gptfdisk-sgdisk     \
    grep                \
    init-restore-mode   \
    kmod                \
    parted              \
    procps              \
    sed                 \
    sysvinit            \
    tar                 \
    util-linux          \
    util-linux-agetty   \
    vim-tiny            \
    "

RDEPENDS_${PN}_append_x64 = "\
    dmidecode           \
    efibootmgr          \
    efivar              \
    fw-printenv         \
    grub                \
    grub-efi            \
    grub-editenv        \
    eudev               \
    ni-smbios-helper    \
    nilrtdiskcrypt      \
    "

RRECOMMENDS_${PN}_x64 = "\
    kernel-module-tpm-tis \
    kernel-module-atkbd   \
    kernel-module-hyperv-keyboard \
    kernel-module-hv-storvsc \
    kernel-module-hv-vmbus \
    kernel-module-hv-utils \
    kernel-module-hv-balloon \
    kernel-module-i8042   \
"
