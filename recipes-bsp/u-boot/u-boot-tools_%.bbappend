PACKAGES += "${PN}-fw-utils"

PROVIDES:xilinx-zynq += "fw-printenv"
RPROVIDES:${PN}-fw-utils:xilinx-zynq = "fw-printenv"
RCONFLICTS:${PN}-fw-utils = "libubootenv-bin"
DEPENDS += "niacctbase"
RDEPENDS:${PN}-fw-utils = "u-boot-env"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += "file://0001-tools-Add-fdtview-a-tool-to-validate-FIT-images.patch"

PACKAGES:append = " ${PN}-fdtview"

do_compile:append() {
	oe_runmake -C ${S} envtools NO_SDL=1 O=${B}
}

do_install:append() {
	install tools/env/fw_printenv ${D}${bindir}/fw_printenv
	ln -rs ${D}${bindir}/fw_printenv ${D}${bindir}/fw_setenv

	# Setup ownership, perms so webserv user can execute fw_printenv
	chmod 4550 ${D}${bindir}/fw_printenv
	chown 0:${LVRT_GROUP} ${D}${bindir}/fw_printenv

	install -d ${D}${base_sbindir}

	ln -rs ${D}${bindir}/fw_printenv ${D}${base_sbindir}/fw_printenv
	ln -rs ${D}${bindir}/fw_setenv ${D}${base_sbindir}/fw_setenv

	# fdtview
	install -m 0755 tools/fdtview ${D}${bindir}/fdtview
	ln -rs ${D}${bindir}/fdtview ${D}${base_sbindir}/fdtview
}

FILES:${PN}-fw-utils = "${bindir}/fw_printenv ${bindir}/fw_setenv ${base_sbindir}/fw_printenv ${base_sbindir}/fw_setenv"
FILES:${PN}-fdtview = " ${bindir}/fdtview ${base_sbindir}/fdtview"
