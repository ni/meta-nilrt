require grub-nilrt.inc

GRUB_BUILDIN:append = " \
    ata \
    btrfs \
    chain \
    echo \
    efi_uga \
    font \
    gfxmenu \
    gfxterm \
    help \
    iorw \
    loopback \
    ls \
    lvm \
    memdisk \
    minicmd \
    multiboot \
    ntfs \
    probe \
    reboot \
    regexp \
    reiserfs \
    search_fs_uuid \
    serial \
    smbios \
    tar \
    terminal \
    terminfo \
    tpm \
    udf \
    xfs \
"

# Downstream NI-branch code quality is not yet ready to build with -Werror
CFLAGS:append = " -Wno-error"

PACKAGES:prepend = "${PN}-nilrt "

do_install:append:class-target() {

    # Build NILRT grub image with prefix=\efi\nilrt instead of
    # the default \EFI\BOOT. We keep the upstream grub image
    # unchanged so that we may use it with USB provisioning tool
    # and other removable storage.
    (
        cd "${B}"
        grub-mkimage \
            --prefix=/efi/nilrt \
            --directory=./grub-core/ \
            --format=${GRUB_TARGET}-efi \
            --output=./${GRUB_IMAGE_PREFIX}nilrt-${GRUB_IMAGE} \
            ${GRUB_BUILDIN}
    )

    # Install NILRT grub image
    install -d ${D}/boot/efi/nilrt
    install -m 644 ${B}/${GRUB_IMAGE_PREFIX}nilrt-${GRUB_IMAGE} ${D}/boot/efi/nilrt/${GRUB_IMAGE}
}

FILES:${PN}-nilrt = "/boot/efi/nilrt/${GRUB_IMAGE}"
