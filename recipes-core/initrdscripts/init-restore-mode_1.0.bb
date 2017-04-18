SUMMARY = "Extremely basic live image init script"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
SRC_URI = "\
	file://init-restore-mode.sh	\
	file://ni_provisioning		\
	file://ni_provisioning.common	\
	file://ni_provisioning.answers.default	\
	"
SRC_URI_append_xilinx-zynqhf = "\
			file://disk_config_xilinx-zynqhf	\
			"
SRC_URI_append_x64 = "\
		file://disk_config_x64		\
		file://grub.cfg			\
		"

RDEPENDS_${PN} += "bash"


RDEPENDS_${PN} = "bash"

S = "${WORKDIR}"

do_install(){
	install -m 0755 ${S}/init-restore-mode.sh ${D}/init
	install -m 0755 ${S}/ni_provisioning ${D}/ni_provisioning
	install -m 0644 ${S}/ni_provisioning.common ${D}/ni_provisioning.common
	install -m 0644 ${S}/ni_provisioning.answers.default ${D}/ni_provisioning.answers.default
}

do_install_append_x64() {
    install -m 0755 ${S}/disk_config_x64 ${D}/disk_config
    install -m 0644 ${S}/grub.cfg ${D}/grub.cfg
}

do_install_append_xilinx-zynqhf() {
    install -m 0755 ${S}/disk_config_xilinx-zynqhf ${D}/disk_config
}

inherit allarch

FILES_${PN} += " /init /ni_provisioning /ni_provisioning.common /ni_provisioning.answers.default /disk_config"
FILES_${PN}_append_x64 += " /grub.cfg "
