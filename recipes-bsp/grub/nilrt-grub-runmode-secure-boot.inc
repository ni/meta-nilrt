# SELoader verifies bootimage.cfg and cpu-mitigations.cfg (sourced by
# bootimage.cfg), so they need detached signatures packaged alongside them.
# Signing here keeps the sidecars in sync when opkg updates this package.

inherit user-key-store

RUNMODE_SIGNED_CFGS = "/boot/runmode/bootimage.cfg /boot/runmode/cpu-mitigations.cfg"

fakeroot python do_sign() {
    if d.getVar('UEFI_SB') != '1':
        return

    image_dir = d.getVar('D')

    for cfg in d.getVar('RUNMODE_SIGNED_CFGS').split():
        uks_bl_sign(image_dir + cfg, d)
}
do_sign[prefuncs] += "check_deploy_keys"
do_sign[prefuncs] += "${@'check_boot_public_key' if d.getVar('GRUB_SIGN_VERIFY') == '1' else ''}"
addtask sign after do_install before do_package

FILES:${PN} += " \
    /boot/runmode/bootimage.cfg${SB_FILE_EXT} \
    /boot/runmode/cpu-mitigations.cfg${SB_FILE_EXT} \
"
CONFFILES:${PN} += " \
    /boot/runmode/bootimage.cfg${SB_FILE_EXT} \
    /boot/runmode/cpu-mitigations.cfg${SB_FILE_EXT} \
"
