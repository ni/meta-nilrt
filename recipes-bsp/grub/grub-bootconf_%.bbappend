FILESEXTRAPATHS_prepend := "${THISDIR}/grub:"

SRC_URI += " \
    file://grub.cfg \
    file://grubenv \
    file://grub.d \
"

PACKAGES_prepend = "${PN}-nilrt"

FILES_${PN}-nilrt      += "/boot/efi/nilrt/grub.cfg /boot/grub/grubenv /bootimage.cfg.d/*.cfg"
CONFFILES_${PN}-nilrt  += "/boot/efi/nilrt/grub.cfg /boot/grub/grubenv /bootimage.cfg.d/*.cfg"

do_install_append () {

    # Install NILRT grub.cfg
    install -d ${D}/boot/efi/nilrt
    install -m 0644 ${S}/grub.cfg ${D}/boot/efi/nilrt/grub.cfg

    # Install empty grubenv
    install -d "${D}/boot/grub"
    install -m 0644 "${S}/grubenv" "${D}/boot/grub/grubenv"

    install -d "${D}/bootimage.cfg.d"
    install -m 0644 ${S}/grub.d/* ${D}/bootimage.cfg.d
}
