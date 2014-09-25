SUMMARY = "Miscellaneous nilrt utilities"
DESCRIPTION = "nilrt distro-specific miscellaneous utilities that provide basic system functionality."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
SECTION = "base"

SRC_URI = "file://status_led \
	   file://nisafemodeversion \
	   file://nicompareversion \
	   file://nisystemformat \
"

SRC_URI_append_arm = " file://fw_env.config"
SRC_URI_append_x64 = " file://fw_printenv"

FILES_${PN} += "/usr/local/natinst/bin/* \
"

DEPENDS += "niacctbase"

RDEPENDS_${PN} += "niacctbase"

group = "${LVRT_GROUP}"

S = "${WORKDIR}"

do_install () {
	install -d ${D}/usr/local/natinst/bin/
	install -d ${D}${sysconfdir}
	install -d ${D}${base_sbindir}
	install -m 0755   ${WORKDIR}/status_led         ${D}/usr/local/natinst/bin
	install -m 0755   ${WORKDIR}/nisafemodeversion         ${D}/usr/local/natinst/bin
	install -m 0755   ${WORKDIR}/nicompareversion         ${D}/usr/local/natinst/bin
	install -m 0550   ${WORKDIR}/nisystemformat         ${D}/usr/local/natinst/bin

	if [ "${TARGET_ARCH}" = "arm" ]; then
		install -m 0644   ${WORKDIR}/fw_env.config         ${D}${sysconfdir}
	fi

	if [ "${TARGET_ARCH}" = "x86_64" ]; then
		install -m 0550   ${WORKDIR}/fw_printenv         ${D}${base_sbindir}
		chown 0:${group} ${D}${base_sbindir}/fw_printenv
		ln -fs fw_printenv ${D}${base_sbindir}/fw_setenv
	fi

	chown 0:${group} ${D}/usr/local/natinst/bin/status_led
	chown 0:${group} ${D}/usr/local/natinst/bin/nisystemformat

}
