SUMMARY = "nilrt specific modification for kdump"
FILESEXTRAPATHS:prepend := "${THISDIR}/kexec-tools:"

SRC_URI += " file://kdump.conf \
           file://bootimage-kdump.cfg \
           "

# Ship the crashkernel setting as a drop-in that is signed at build time, since
# bootimage.cfg must not be modified on the target once it is signed.
inherit user-key-store

KDUMP_RUNMODE_CFG = "/boot/runmode/bootimage.cfg.d/kdump.cfg"

do_install:append() {
    install -d ${D}/boot/runmode/bootimage.cfg.d
    install -m 0644 ${UNPACKDIR}/bootimage-kdump.cfg ${D}${KDUMP_RUNMODE_CFG}
}

fakeroot python do_sign() {
    if d.getVar('UEFI_SB') != '1':
        return

    uks_bl_sign(d.getVar('D') + d.getVar('KDUMP_RUNMODE_CFG'), d)
}
do_sign[prefuncs] += "check_deploy_keys"
do_sign[prefuncs] += "${@'check_boot_public_key' if d.getVar('GRUB_SIGN_VERIFY') == '1' else ''}"
fakeroot python do_sign() {
    if d.getVar('UEFI_SB') != '1':
        return

    uks_bl_sign(d.getVar('D') + d.getVar('KDUMP_RUNMODE_CFG'), d)
}
do_sign[prefuncs] += "check_deploy_keys"
do_sign[prefuncs] += "${@'check_boot_public_key' if d.getVar('GRUB_SIGN_VERIFY') == '1' else ''}"
addtask sign after do_install before do_package

pkg_postinst_ontarget:kdump() {
    # The drop-in changes PCR 15, so LUKS sealed to PCR 15 needs a reseal before reboot.
    sb_var=/sys/firmware/efi/efivars/SecureBoot-8be4df61-93ca-11d2-aa0d-00e098032b8c
    if [ -r "$sb_var" ] && [ "$(od -An -j4 -N1 -tu1 "$sb_var" | tr -d '[:space:]')" = "1" ]; then
        echo "[kdump:warning] Secure Boot is enabled. Run ni-reseal-luks before rebooting, or the encrypted volumes may fail to unlock."
    fi
}

pkg_postrm:kdump() {
    if [ -n "$D" ]; then
        exit 0
    fi
    sb_var=/sys/firmware/efi/efivars/SecureBoot-8be4df61-93ca-11d2-aa0d-00e098032b8c
    if [ -r "$sb_var" ] && [ "$(od -An -j4 -N1 -tu1 "$sb_var" | tr -d '[:space:]')" = "1" ]; then
        echo "[kdump:warning] Secure Boot is enabled. Run ni-reseal-luks before rebooting, or the encrypted volumes may fail to unlock."
    fi
}

FILES:kdump += " \
    ${KDUMP_RUNMODE_CFG} \
    ${KDUMP_RUNMODE_CFG}${SB_FILE_EXT} \
"
CONFFILES:kdump += " \
    ${KDUMP_RUNMODE_CFG} \
    ${KDUMP_RUNMODE_CFG}${SB_FILE_EXT} \
"
