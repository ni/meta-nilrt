SUMMARY = "Extremely basic live image init script"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "\
	file://init-restore-mode.sh \
	file://ni_provisioning \
	file://ni_provisioning.common \
	file://ni_provisioning.answers.default \
"

SRC_URI_append_xilinx-zynqhf = "\
	file://disk_config_xilinx-zynqhf \
"

SRC_URI_append_x64 = "\
	file://ni_provisioning.safemode \
	file://disk_config_x64 \
	file://grub.cfg	\
"

RDEPENDS_${PN} += "bash"

do_install() {
	install -m 0755 ${WORKDIR}/init-restore-mode.sh ${D}/init
	install -m 0755 ${WORKDIR}/ni_provisioning ${D}/ni_provisioning
	install -m 0644 ${WORKDIR}/ni_provisioning.common ${D}/ni_provisioning.common
	install -m 0644 ${WORKDIR}/ni_provisioning.answers.default ${D}/ni_provisioning.answers.default
}

do_install_append_x64() {
	install -m 0644 ${WORKDIR}/ni_provisioning.safemode ${D}/ni_provisioning.safemode
	install -m 0755 ${WORKDIR}/disk_config_x64 ${D}/disk_config
	install -m 0644 ${WORKDIR}/grub.cfg ${D}/grub.cfg
}

do_install_append_xilinx-zynqhf() {
	install -m 0755 ${WORKDIR}/disk_config_xilinx-zynqhf ${D}/disk_config
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

FILES_${PN} += " /init /ni_provisioning* /disk_config"
FILES_${PN}_append_x64 += " /grub.cfg "
