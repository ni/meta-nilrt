SUMMARY = "fw_printenv/fw_setenv utility"
DESCRIPTION = "distro-specific fw_printenv utility for reading firmware variables."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
SECTION = "base"

SRC_URI = " "

SRC_URI_append_x64 = " file://fw_printenv \
                       file://EFI_NI_vars \
                       file://SMBIOS_NI_vars \
                       file://grubvar_readonly \
"

# Add an empty FILES assignment because the architecture-specific
# FILES_append will fail if FILES is not previously defined.
FILES_${PN} += ""

FILES_${PN}_append_x64 = "${datadir}/fw_printenv/* \
"

DEPENDS += "niacctbase \
"
RDEPENDS_${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'x86', 'grub-editenv', '', d)} \
    niacctbase \
    bash \
"

group = "${LVRT_GROUP}"

S = "${WORKDIR}"

fw_printenv_dir = "${datadir}/fw_printenv"

do_install () {
	if [ "${TARGET_ARCH}" = "x86_64" ]; then
		install -d ${D}${base_sbindir}
		install -d ${D}${fw_printenv_dir}
		install -m 0550   ${WORKDIR}/fw_printenv         ${D}${base_sbindir}
		sed -i -e 's,@FW_PRINTENV_DIR@,${fw_printenv_dir},g' ${D}${base_sbindir}/fw_printenv

		chown 0:${group} ${D}${base_sbindir}/fw_printenv
		ln -fs fw_printenv ${D}${base_sbindir}/fw_setenv

		install -m 0444   ${WORKDIR}/EFI_NI_vars         ${D}${fw_printenv_dir}
		install -m 0444   ${WORKDIR}/SMBIOS_NI_vars      ${D}${fw_printenv_dir}
		install -m 0444   ${WORKDIR}/grubvar_readonly    ${D}${fw_printenv_dir}
	fi
}
