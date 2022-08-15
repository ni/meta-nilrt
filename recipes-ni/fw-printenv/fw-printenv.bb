SUMMARY = "fw_printenv/fw_setenv utility"
DESCRIPTION = "NI-LinuxRT-specific fw_printenv utility for reading firmware variables."
HOMEPAGE = "https://github.com/ni/meta-nilrt"
SECTION = "base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

COMPATIBLE_MACHINE = "x64"
DEPENDS = "niacctbase"

PV = "2.0"


SRC_URI = "\
	file://EFI_NI_vars \
	file://SMBIOS_NI_vars \
	file://fw_printenv \
	file://grubvar_readonly \
"

S = "${WORKDIR}"


do_install () {
	install -d ${D}${base_sbindir}
	install -m 0550 ${S}/fw_printenv ${D}${base_sbindir}
	sed -i -e 's,@FW_PRINTENV_DIR@,${datadir}/fw_printenv,g' ${D}${base_sbindir}/fw_printenv
	chown 0:${LVRT_GROUP} ${D}${base_sbindir}/fw_printenv
	ln -fs fw_printenv ${D}${base_sbindir}/fw_setenv

	install -d ${D}${datadir}/fw_printenv
	install -m 0444 ${S}/EFI_NI_vars      ${D}${datadir}/fw_printenv
	install -m 0444 ${S}/SMBIOS_NI_vars   ${D}${datadir}/fw_printenv
	install -m 0444 ${S}/grubvar_readonly ${D}${datadir}/fw_printenv
}


FILES:${PN} += "${datadir}/fw_printenv/*"
RDEPENDS:${PN} += "\
	bash \
	grub-editenv \
	ni-smbios-helper \
	niacctbase \
"
