FILESEXTRAPATHS_prepend := "${THISDIR}/grub:"

SRC_URI += " \
    file://grub.cfg \
    file://grubenv \
"

PACKAGES_prepend = "${PN}-nilrt"

FILES_${PN}-nilrt      += "/boot/efi/nilrt/grub.cfg /boot/grub/grubenv"
CONFFILES_${PN}-nilrt  += "/boot/efi/nilrt/grub.cfg /boot/grub/grubenv"

do_install_append () {

    # Install NILRT grub.cfg
    install -d ${D}/boot/efi/nilrt
    install -m 0644 ${S}/grub.cfg ${D}/boot/efi/nilrt/grub.cfg

    # Install empty grubenv
    install -d "${D}/boot/grub"
    install -m 0644 "${S}/grubenv" "${D}/boot/grub/grubenv"
}
