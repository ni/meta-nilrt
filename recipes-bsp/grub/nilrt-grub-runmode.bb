SUMMARY = "nilrt runmode grub configuration"
DESCRIPTION = "nilrt distro-specific runmode boot files"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

FILESEXTRAPATHS:prepend := "${THISDIR}/grub:"

SRC_URI += " \
    file://grub-runmode-bootimage.cfg \
    file://grub.d \
    file://run-ptest \
    file://ptest-format.sh \
    file://test-grubcfg.sh \
"

inherit ptest

FILES:${PN}     += "/boot/runmode/bootimage.cfg /boot/runmode/bootimage.cfg.d/*.cfg /boot/runmode/cpu-mitigations.cfg"
CONFFILES:${PN} += "/boot/runmode/bootimage.cfg /boot/runmode/bootimage.cfg.d/*.cfg /boot/runmode/cpu-mitigations.cfg"

RDEPENDS:${PN}-ptest += "bash"

do_install () {
	install -d ${D}/boot/runmode
	install -m 0644 ${WORKDIR}/grub-runmode-bootimage.cfg ${D}/boot/runmode/bootimage.cfg
	install -m 0644 ${WORKDIR}/grub.d/cpu-mitigations.cfg ${D}/boot/runmode
}

do_install_ptest:append () {
    install -m 0644 ${WORKDIR}/ptest-format.sh ${D}${PTEST_PATH}
    install -m 0755 ${WORKDIR}/run-ptest       ${D}${PTEST_PATH}
    install -m 0755 ${WORKDIR}/test-grubcfg.sh ${D}${PTEST_PATH}
}
