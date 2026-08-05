# Secure-boot handling for the NI-prefixed GRUB image installed under
# ${NILRT_EFI_DIR}. meta-efi-secure-boot only signs the image it installs to
# ${EFI_BOOT_PATH}, so the NI variant has to be signed here with the same key.

inherit user-key-store

NILRT_EFI_DIR = "/boot/efi/nilrt"

python do_sign:append:class-target() {
    if d.getVar('UEFI_SB') != '1':
        return

    nilrt_dir = d.getVar('D') + d.getVar('NILRT_EFI_DIR') + '/'
    grub_image = nilrt_dir + d.getVar('GRUB_IMAGE')

    sb_sign(grub_image, grub_image, d)
}
