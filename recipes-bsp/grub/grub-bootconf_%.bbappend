FILESEXTRAPATHS:prepend := "${THISDIR}/grub:"

SRC_URI += " \
    file://grub.cfg \
    file://grubenv \
    file://grub.d \
"

PACKAGES:prepend = "${PN}-nilrt "

FILES:${PN}-nilrt      += "/boot/efi/nilrt/grub.cfg /boot/grub/grubenv /boot/bootimage.cfg.d/*.cfg"
CONFFILES:${PN}-nilrt  += "/boot/efi/nilrt/grub.cfg /boot/grub/grubenv /boot/bootimage.cfg.d/*.cfg"

do_install:append () {

    # Install NILRT grub.cfg
    install -d ${D}/boot/efi/nilrt
    install -m 0644 ${S}/grub.cfg ${D}/boot/efi/nilrt/grub.cfg

    # Install empty grubenv
    install -d "${D}/boot/grub"
    install -m 0644 "${S}/grubenv" "${D}/boot/grub/grubenv"

    install -d "${D}/boot/bootimage.cfg.d"
    install -m 0644 ${S}/grub.d/* ${D}/boot/bootimage.cfg.d
}
