SUMMARY = "NILRT safemode grub configuration"
DESCRIPTION = "NILRT distro-specific safemode boot files"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

FILESEXTRAPATHS:prepend := "${THISDIR}/grub:"

require ${@bb.utils.contains('DISTRO_FEATURES', 'efi-secure-boot', 'nilrt-grub-safemode-secure-boot.inc', '', d)}

SAFEMODE_GRUB_CFG = "${@bb.utils.contains('DISTRO_FEATURES', 'efi-secure-boot', 'grub-safemode-secure-boot.cfg', 'grub-safemode.cfg', d)}"

SRC_URI += " \
    file://grubenv \
    file://${SAFEMODE_GRUB_CFG} \
    file://grub-safemode-bootimage.cfg \
"

FILES:${PN} += " \
    /boot/bootimage.cfg \
    /boot/grub.cfg \
    /boot/grubenv \
"

CONFFILES:${PN} += " \
    /boot/bootimage.cfg \
    /boot/grub.cfg \
    /boot/grubenv \
"

do_install () {
	install -d ${D}/boot
	install -m 0644 ${WORKDIR}/grub-safemode-bootimage.cfg ${D}/boot/bootimage.cfg
    install -m 0644 ${WORKDIR}/${SAFEMODE_GRUB_CFG} ${D}/boot/grub.cfg
	install -m 0644 ${WORKDIR}/grubenv ${D}/boot/grubenv
}
