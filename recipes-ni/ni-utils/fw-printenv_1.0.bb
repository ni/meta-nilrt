SUMMARY = "fw_printenv/fw_setenv utility for x64 systems"
DESCRIPTION = "distro-specific fw_printenv utility for reading firmware variables."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = "\
	file://fw_printenv \
	file://EFI_NI_vars \
	file://SMBIOS_NI_vars \
	file://grubvar_readonly \
"

COMPATIBLE_MACHINE = "x64"

FILES_${PN} += "${datadir}/fw_printenv/*"

DEPENDS += "shadow-native pseudo-native niacctbase"

RDEPENDS_${PN} += "ni-smbios-helper grub-editenv niacctbase bash"

S = "${WORKDIR}"

do_install () {
	install -d ${D}${base_sbindir}
	install -d ${D}${datadir}/fw_printenv

	install -m 0550   ${WORKDIR}/fw_printenv         ${D}${base_sbindir}
	sed -i -e 's,@FW_PRINTENV_DIR@,${datadir}/fw_printenv,g' ${D}${base_sbindir}/fw_printenv

	chown 0:${LVRT_GROUP} ${D}${base_sbindir}/fw_printenv
	ln -fs fw_printenv ${D}${base_sbindir}/fw_setenv

	install -m 0444   ${WORKDIR}/EFI_NI_vars         ${D}${datadir}/fw_printenv
	install -m 0444   ${WORKDIR}/SMBIOS_NI_vars      ${D}${datadir}/fw_printenv
	install -m 0444   ${WORKDIR}/grubvar_readonly    ${D}${datadir}/fw_printenv
}
