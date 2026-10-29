# Secure-boot handling for the NI-prefixed GRUB image installed under
# ${NILRT_EFI_DIR}. meta-efi-secure-boot only signs the image it installs to
# ${EFI_BOOT_PATH}, so the NI variant has to be signed here with the same key.

inherit user-key-store

NILRT_EFI_DIR = "/boot/efi/nilrt"

python __anonymous() {
    if d.getVar('UEFI_SELOADER') == '1':
        expected_ext = '.p7b'
        verifier = 'SELoader PKCS#7 buffer verification'
    elif d.getVar('GRUB_SIGN_VERIFY') == '1':
        expected_ext = '.sig'
        verifier = 'GRUB GPG verification'
    else:
        return

    if d.getVar('SB_FILE_EXT') != expected_ext:
        bb.fatal('%s requires SB_FILE_EXT="%s"; got %r. '
                 '.sig signatures require UEFI_SELOADER="0" and '
                 'GRUB_SIGN_VERIFY="1".' %
                 (verifier, expected_ext, d.getVar('SB_FILE_EXT')))
}

python do_sign:append:class-target() {
    if d.getVar('UEFI_SB') != '1':
        return

    nilrt_dir = d.getVar('D') + d.getVar('NILRT_EFI_DIR') + '/'
    grub_image = nilrt_dir + d.getVar('GRUB_IMAGE')

    sb_sign(grub_image, grub_image, d)
}
