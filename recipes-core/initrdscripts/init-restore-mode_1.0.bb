SUMMARY = "Extremely basic live image init script"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "\
	file://00-init-restore-mode.sh \
	file://efifix \
	file://init-restore-mode.sh \
	file://ni_provisioning \
	file://ni_provisioning.answers.default \
	file://ni_provisioning.common \
"

SRC_URI_append_xilinx-zynqhf = "\
	file://disk_config_xilinx-zynqhf \
"

SRC_URI_append_x64 = "\
	file://disk_config_x64 \
	file://grub.cfg	\
	file://ni_provisioning.safemode \
	file://ptest \
"

RDEPENDS_${PN} += "bash rauc"

inherit ptest

RDEPENDS_${PN}-ptest += "bash efibootmgr"
# The ptests should be run on a system which has already been provisioned, so a
# dependency on the provisioning scripts is not necessary.
RDEPENDS_${PN}-ptest_remove += "${PN}"

do_install() {
	install -m 0755 ${WORKDIR}/init-restore-mode.sh ${D}/init
	install -m 0755 ${WORKDIR}/efifix ${D}/
	install -m 0755 ${WORKDIR}/ni_provisioning ${D}/
	install -m 0644 ${WORKDIR}/ni_provisioning.common ${D}/
	install -m 0644 ${WORKDIR}/ni_provisioning.answers.default ${D}/

	install -d ${D}/${sysconfdir}/profile.d
	install -m 0644 ${WORKDIR}/00-init-restore-mode.sh ${D}/${sysconfdir}/profile.d/
}

do_install_append_x64() {
	install -m 0644 ${WORKDIR}/ni_provisioning.safemode ${D}/
	install -m 0755 ${WORKDIR}/disk_config_x64 ${D}/disk_config
	install -m 0644 ${WORKDIR}/grub.cfg ${D}/
}

do_install_append_xilinx-zynqhf() {
	install -m 0755 ${WORKDIR}/disk_config_xilinx-zynqhf ${D}/disk_config
}

do_install_ptest_append_x64() {
	install -m 0755 ${WORKDIR}/ptest/run-ptest               ${D}${PTEST_PATH}
	install -m 0755 ${WORKDIR}/ptest/test_ni_provisioning.sh ${D}${PTEST_PATH}
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

FILES_${PN} += " /init /ni_provisioning* /disk_config /efifix /etc/profile.d/00-init-restore-mode.sh"
FILES_${PN}_append_x64 += " /grub.cfg "
