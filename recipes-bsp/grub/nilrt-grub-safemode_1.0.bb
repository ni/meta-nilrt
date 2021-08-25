SUMMARY = "NILRT safemode grub configuration"
DESCRIPTION = "NILRT distro-specific safemode boot files"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

FILESEXTRAPATHS_prepend := "${THISDIR}/grub:"

SRC_URI += " \
    file://grubenv \
    file://grub-safemode.cfg \
    file://grub-safemode-bootimage.cfg \
"

FILES_${PN} += " \
    /boot/bootimage.cfg \
    /boot/grub.cfg \
    /boot/grubenv \
"

CONFFILES_${PN} += " \
    /boot/bootimage.cfg \
    /boot/grub.cfg \
    /boot/grubenv \
"

do_install () {
	install -d ${D}/boot
	install -m 0644 ${WORKDIR}/grub-safemode-bootimage.cfg ${D}/boot/bootimage.cfg
	install -m 0644 ${WORKDIR}/grub-safemode.cfg ${D}/boot/grub.cfg
	install -m 0644 ${WORKDIR}/grubenv ${D}/boot/grubenv
}
