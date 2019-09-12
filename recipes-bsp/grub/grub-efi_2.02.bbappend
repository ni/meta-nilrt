require grub-nilrt.inc

GRUB_BUILDIN += "smbios chain multiboot efi_uga font gfxterm gfxmenu terminal \
                minicmd iorw echo reboot terminfo loopback memdisk tar help serial \
                ls search_fs_uuid udf btrfs ntfs reiserfs xfs lvm ata tpm measure \
                regexp probe"

# Downstream NI-branch code quality is not yet ready to build with -Werror
CFLAGS_append += "-Wno-error"

PACKAGES_prepend = "${PN}-nilrt"

do_install_append_class-target() {

    # Build NILRT grub image with prefix=\efi\nilrt instead of
    # the default \EFI\BOOT. We keep the upstream grub image
    # unchanged so that we may use it with USB provisioning tool
    # and other removable storage.
    (
    cd "${B}"
    grub-mkimage -p /efi/nilrt -d ./grub-core/ \
                 -O ${GRUB_TARGET}-efi -o ./${GRUB_IMAGE_PREFIX}nilrt-${GRUB_IMAGE} \
                 ${GRUB_BUILDIN}
    )

    # Install NILRT grub image
    install -d ${D}/boot/efi/nilrt
    install -m 644 ${B}/${GRUB_IMAGE_PREFIX}nilrt-${GRUB_IMAGE} ${D}/boot/efi/nilrt/${GRUB_IMAGE}
}

FILES_${PN}-nilrt = "/boot/efi/nilrt/${GRUB_IMAGE}"
