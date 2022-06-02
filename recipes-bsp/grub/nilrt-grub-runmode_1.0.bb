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

FILES_${PN}     += "/boot/runmode/bootimage.cfg /boot/runmode/bootimage.cfg.d/*.cfg /boot/runmode/cve-*.cfg"
CONFFILES_${PN} += "/boot/runmode/bootimage.cfg /boot/runmode/bootimage.cfg.d/*.cfg /boot/runmode/cve-*.cfg"

do_install () {
	install -d ${D}/boot/runmode
	install -m 0644 ${WORKDIR}/grub-runmode-bootimage.cfg ${D}/boot/runmode/bootimage.cfg

	install -m 0644 ${WORKDIR}/grub.d/cve-2017-5715.cfg ${D}/boot/runmode
	install -m 0644 ${WORKDIR}/grub.d/cve-2017-5754.cfg ${D}/boot/runmode
	install -m 0644 ${WORKDIR}/grub.d/cve-2018-3620_3646.cfg ${D}/boot/runmode
	install -m 0644 ${WORKDIR}/grub.d/cve-2018-3639.cfg ${D}/boot/runmode
}
