COMPATIBLE_MACHINE = "xilinx-zynq"
PROVIDES += "fw-printenv"
DEPENDS += "niacctbase"
RPROVIDES:${PN}-bin += "fw-printenv"
RDEPENDS:${PN}-bin += "u-boot-env"

do_install:append() {
	# Setup ownership, perms so webserv user can execute fw_printenv
	chmod 4550 ${D}${bindir}/fw_printenv
	chown 0:${LVRT_GROUP} ${D}${bindir}/fw_printenv

	install -d ${D}${base_sbindir}

	ln -s ${bindir}/fw_printenv ${D}${base_sbindir}/fw_printenv
	ln -s ${bindir}/fw_setenv ${D}${base_sbindir}/fw_setenv
}

FILES:${PN}-bin += "\
	${base_sbindir}/fw_printenv \
	${base_sbindir}/fw_setenv \
"
