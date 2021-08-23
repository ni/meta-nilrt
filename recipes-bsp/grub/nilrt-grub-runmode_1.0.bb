SUMMARY = "nilrt runmode grub configuration"
DESCRIPTION = "nilrt distro-specific runmode boot files"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

FILESEXTRAPATHS_prepend := "${THISDIR}/grub:"

SRC_URI += " \
    file://grub-runmode-bootimage.cfg \
    file://grub.d \
"

FILES_${PN}     += "/boot/runmode/bootimage.cfg /boot/runmode/bootimage.cfg.d/*.cfg"
CONFFILES_${PN} += "/boot/runmode/bootimage.cfg /boot/runmode/bootimage.cfg.d/*.cfg"

do_install () {
	install -d ${D}/boot/runmode
	install -m 0644 ${WORKDIR}/grub-runmode-bootimage.cfg ${D}/boot/runmode/bootimage.cfg

	install -d "${D}/boot/runmode/bootimage.cfg.d"
	install -m 0644 ${WORKDIR}/grub.d/* ${D}/boot/runmode/bootimage.cfg.d
}
