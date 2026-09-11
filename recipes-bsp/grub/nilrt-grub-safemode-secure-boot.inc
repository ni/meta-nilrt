# SELoader verifies every config grub loads, so the safemode configs need
# detached signatures alongside them on the ESP and bootfs partitions.

inherit user-key-store

SAFEMODE_SIGNED_CFGS = "/boot/grub.cfg /boot/bootimage.cfg"

fakeroot python do_sign() {
    if d.getVar('UEFI_SB') != '1':
        return

    image_dir = d.getVar('D')

    for cfg in d.getVar('SAFEMODE_SIGNED_CFGS').split():
        uks_bl_sign(image_dir + cfg, d)
}
do_sign[prefuncs] += "check_deploy_keys"
addtask sign after do_install before do_package

FILES:${PN} += " \
    /boot/bootimage.cfg${SB_FILE_EXT} \
    /boot/grub.cfg${SB_FILE_EXT} \
"
