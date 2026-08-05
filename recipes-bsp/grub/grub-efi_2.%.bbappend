require grub-nilrt.inc
require ${@bb.utils.contains('DISTRO_FEATURES', 'efi-secure-boot', 'grub-efi-nilrt-secure-boot.inc', '', d)}

# UEFI_SELOADER is set by the meta-signing-key layer.conf whether or not
# efi-secure-boot is enabled, so the mok2verify patches are only actually in
# SRC_URI when both conditions hold.
GRUB_NILRT_MOK2VERIFY = "${@'1' if bb.utils.contains('DISTRO_FEATURES', 'efi-secure-boot', True, False, d) and d.getVar('UEFI_SELOADER') == '1' else '0'}"
GRUB_NILRT_ADVERTISE_PATCH = "${@'file://grub-advertise-NI-NILRT-over-GNU-GRUB-mok2verify.patch' if d.getVar('GRUB_NILRT_MOK2VERIFY') == '1' else 'file://grub-advertise-NI-NILRT-over-GNU-GRUB.patch'}"

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

PACKAGES:prepend = "${PN}-nilrt "

do_install:append:class-target() {

    # Build NILRT grub image with prefix=\efi\nilrt instead of
    # the default \EFI\BOOT. We keep the upstream grub image
    # unchanged so that we may use it with USB provisioning tool
    # and other removable storage.
    # GRUB_SECURE_BUILDIN is empty unless efi-secure-boot is enabled; it carries
    # mok2verify/efivar and the SBAT section the signed chain requires.
    (
        cd "${B}"
        grub-mkimage \
            --prefix=/efi/nilrt \
            --directory=./grub-core/ \
            --format=${GRUB_TARGET}-efi \
            --output=./${GRUB_IMAGE_PREFIX}nilrt-${GRUB_IMAGE} \
            ${GRUB_BUILDIN} ${GRUB_SECURE_BUILDIN}
    )

    # Install NILRT grub image
    install -d ${D}/boot/efi/nilrt
    install -m 644 ${B}/${GRUB_IMAGE_PREFIX}nilrt-${GRUB_IMAGE} ${D}/boot/efi/nilrt/${GRUB_IMAGE}
}

FILES:${PN}-nilrt = "/boot/efi/nilrt/${GRUB_IMAGE}"
