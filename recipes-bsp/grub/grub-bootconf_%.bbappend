FILESEXTRAPATHS_prepend := "${THISDIR}/grub:"

SRC_URI += " \
    file://grub.cfg \
    file://grubenv \
"

build_efi_cfg() {
    # Copy grub.cfg to grub-bootconf because do_install from
    # openembedded-core recipe expects it there.
    install -m 0644 ${S}/grub.cfg ${WORKDIR}/grub-bootconf
}

FILES_${PN} += "/boot/grub/grubenv"
CONFFILES_${PN} += "/boot/grub/grubenv"

do_install_append () {

    # Append empty grubenv
    install -d "${D}/boot/grub"
    install -m 0644 "${S}/grubenv" "${D}/boot/grub/grubenv"
}
