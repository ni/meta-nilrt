SUMMARY = "Miscellaneous nilrt utilities"
DESCRIPTION = "nilrt distro-specific miscellaneous utilities that provide basic system functionality."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
SECTION = "base"

SRC_URI = "file://status_led \
	   file://nicompareversion \
	   file://nisetbootmode.functions \
	   file://nisetbootmode \
"

SRC_URI_append_arm = " file://fw_env.config"
SRC_URI_append_x64 = " file://fw_printenv \
                       file://EFI_NI_vars \
                       file://SMBIOS_NI_vars \
                       file://grubvar_readonly \
"

FILES_${PN} += "\
	/usr/lib/nisetbootmode.functions \
"

FILES_${PN}_append_x64 = "${datadir}/fw_printenv/* \
"

DEPENDS += "niacctbase"

RDEPENDS_${PN} += "niacctbase \
	${@base_contains("MACHINE_FEATURES", "x86", "grub-editenv", "", d)} \
"

group = "${LVRT_GROUP}"

S = "${WORKDIR}"

fw_printenv_dir = "${datadir}/fw_printenv"

do_install () {
	install -d ${D}${bindir}
	install -d ${D}${sysconfdir}
	install -d ${D}${base_sbindir}
	install -d ${D}${fw_printenv_dir}
	install -d ${D}${libdir}
	install -m 0755   ${WORKDIR}/status_led         ${D}${bindir}
	install -m 0755   ${WORKDIR}/nicompareversion   ${D}${bindir}
	install -m 0440   ${WORKDIR}/nisetbootmode.functions         ${D}${libdir}
	install -m 0550   ${WORKDIR}/nisetbootmode         ${D}${bindir}

	if [ "${TARGET_ARCH}" = "arm" ]; then
		install -m 0644   ${WORKDIR}/fw_env.config         ${D}${sysconfdir}
	fi

	if [ "${TARGET_ARCH}" = "x86_64" ]; then
		install -m 0550   ${WORKDIR}/fw_printenv         ${D}${base_sbindir}
		sed -i -e 's,@FW_PRINTENV_DIR@,${fw_printenv_dir},g' ${D}${base_sbindir}/fw_printenv

		chown 0:${group} ${D}${base_sbindir}/fw_printenv
		ln -fs fw_printenv ${D}${base_sbindir}/fw_setenv
		install -m 0444   ${WORKDIR}/EFI_NI_vars         ${D}${fw_printenv_dir}
		install -m 0444   ${WORKDIR}/SMBIOS_NI_vars      ${D}${fw_printenv_dir}
		install -m 0444   ${WORKDIR}/grubvar_readonly    ${D}${fw_printenv_dir}
	fi

	chown 0:${group} ${D}${bindir}/status_led

}
