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
addtask sign after do_install before do_package

FILES:${PN}-nilrt += "/boot/efi/nilrt/grub.cfg${SB_FILE_EXT}"
