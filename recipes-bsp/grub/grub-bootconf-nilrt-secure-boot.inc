# SELoader verifies every config grub loads (including via the buffer-verify
# path used for sourced menus, see grub-efi.cfg), so the NILRT-maintained
# runmode menu needs a detached signature alongside it on the ESP.

inherit user-key-store

fakeroot python do_sign() {
    if d.getVar('UEFI_SB') != '1':
        return

    uks_bl_sign(d.getVar('D') + '/boot/efi/nilrt/grub.cfg', d)
}
do_sign[prefuncs] += "check_deploy_keys"
do_sign[prefuncs] += "${@'check_boot_public_key' if d.getVar('GRUB_SIGN_VERIFY') == '1' else ''}"
addtask sign after do_install before do_package

FILES:${PN}-nilrt += "/boot/efi/nilrt/grub.cfg${SB_FILE_EXT}"
