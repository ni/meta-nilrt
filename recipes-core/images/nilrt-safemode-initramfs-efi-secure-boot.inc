DEPENDS += "openssl-native"

inherit user-key-store

fakeroot python do_sign_secure_boot() {
    import os

    if d.getVar('UEFI_SB') != '1':
        return

    deploy_dir = d.getVar('DEPLOY_DIR_IMAGE')
    image_link = d.getVar('IMAGE_LINK_NAME')

    for image_fstype in d.getVar('IMAGE_FSTYPES').split():
        artifact = os.path.join(deploy_dir, image_link + '.' + image_fstype)
        if os.path.exists(artifact):
            uks_bl_sign(artifact, d)
}

addtask sign_secure_boot after do_image_complete before do_build

do_sign_secure_boot[prefuncs] += "check_deploy_keys"
do_sign_secure_boot[prefuncs] += "${@'check_boot_public_key' if d.getVar('GRUB_SIGN_VERIFY') == '1' else ''}"
do_sign_secure_boot[depends] += " \
    openssl-native:do_populate_sysroot \
    sbsigntool-native:do_populate_sysroot \
    libsign-native:do_populate_sysroot \
    efitools-native:do_populate_sysroot \
    gnupg-native:do_populate_sysroot \
"
