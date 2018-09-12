require grub-nilrt.inc

GRUB_BUILDIN += "smbios chain multiboot efi_uga font gfxterm gfxmenu terminal \
                minicmd iorw echo reboot terminfo loopback memdisk tar help serial \
                ls search_fs_uuid udf btrfs ntfs reiserfs xfs lvm ata tpm measure"

GRUB_NILRT_IMAGE = "grubx64.efi"

PACKAGES =+ "${PN}-nilrt"
PROVIDES =+ "${PN}-nilrt"

# we package 'grub-efi-nilrt' with a non-standard name & path under /boot/grubx64.efi
# in the rootfs because we expect to chainload it from the 'normal OE-built' grub-efi
# which is installed under the standard UEFI path /EFI/BOOT/ on the bootfs partition.
# We can't modify this because our customers also chainload the distro depending on
# the existing path from their own bootloader config (they dual boot with Windows).
do_install_append_class-target() {
    cp ${D}/boot/EFI/BOOT/${GRUB_IMAGE} ${D}/boot/${GRUB_NILRT_IMAGE}
    install -m 0644 ${WORKDIR}/grub.cfg ${D}/boot/
}

FILES_${PN}-nilrt = "/boot/${GRUB_NILRT_IMAGE} \
                     /boot/grub.cfg"

# Downstream NI-branch code quality is not yet ready to build with -Werror
CFLAGS_append += "-Wno-error"
