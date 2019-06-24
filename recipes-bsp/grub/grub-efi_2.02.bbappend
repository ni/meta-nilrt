require grub-nilrt.inc

GRUB_BUILDIN += "smbios chain multiboot efi_uga font gfxterm gfxmenu terminal \
                minicmd iorw echo reboot terminfo loopback memdisk tar help serial \
                ls search_fs_uuid udf btrfs ntfs reiserfs xfs lvm ata tpm measure \
                regexp probe"

# package 'grub-efi-rootfs-chainloaded' with non-standard pathname /boot/grubx64.efi
# in the rootfs because we expect to chainload it from the 'normal OE-built' grub-efi
# which is installed under the standard UEFI path /EFI/BOOT/ on the bootfs partition.
# We can't modify this because our customers also chainload the distro depending on
# the existing path from their own bootloader config (they dual boot with Windows).
PACKAGES =+ "grub-efi-rootfs-chainloaded"
PROVIDES =+ "grub-efi-rootfs-chainloaded"

GRUB_CHAINLOADED_IMAGE = "grubx64.efi"

do_install_append_class-target() {
    # need to compile custom grub image because of the non-standard /boot/ prefix
    grub-mkimage -p /boot/ -O ${GRUB_TARGET}-efi -d ./grub-core/ \
        -o ${D}/boot/${GRUB_CHAINLOADED_IMAGE} ${GRUB_BUILDIN}

    install -m 0644 ${WORKDIR}/grub.cfg ${D}/boot/

    install -m 0744 -d ${D}/boot/grub.d
    install -m 0644 ${WORKDIR}/grub.d/* ${D}/boot/grub.d/
}

FILES_grub-efi-rootfs-chainloaded = "\
    /boot/${GRUB_CHAINLOADED_IMAGE} \
    /boot/grub.cfg \
    /boot/grub.d/* \
"

# Downstream NI-branch code quality is not yet ready to build with -Werror
CFLAGS_append += "-Wno-error"
