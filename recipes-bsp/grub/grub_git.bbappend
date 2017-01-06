require grub-nilrt.inc

PACKAGES =+ "grub-editenv"

RDEPENDS_${PN} += "grub-editenv"

FILES_grub-editenv = "${bindir}/grub-editenv"
