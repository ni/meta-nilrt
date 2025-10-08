COMPATIBLE_MACHINE = "xilinx-zynq"
PROVIDES += "fw-printenv"
RPROVIDES:${PN}-bin += "fw-printenv"
RDEPENDS:${PN}-bin += "u-boot-env"

do_install:append() {
	install -d ${D}${base_sbindir}

	ln -s ${bindir}/fw_printenv ${D}${base_sbindir}/fw_printenv
	ln -s ${bindir}/fw_setenv ${D}${base_sbindir}/fw_setenv
}

FILES:${PN}-bin += "\
	${base_sbindir}/fw_printenv \
	${base_sbindir}/fw_setenv \
"
