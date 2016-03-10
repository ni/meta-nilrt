SUMMARY = "Miscellaneous nilrt utilities"
DESCRIPTION = "nilrt distro-specific miscellaneous utilities that provide basic system functionality."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
SECTION = "base"

SRC_URI = "file://status_led \
	   file://nisetbootmode.functions \
	   file://nisetbootmode \
	   file://ninetcfgutil \
"

SRC_URI_append_arm = " file://fw_env.config file://ninetcfgutil_platdep.sh"
SRC_URI_append_x64 = " file://ninetcfgutil_platdep.sh \
"

FILES_${PN} += "\
	/usr/lib/nisetbootmode.functions \
	/usr/lib/ninetcfgutil_platdep.sh \
"

DEPENDS += "niacctbase"

RDEPENDS_${PN} += "niacctbase \
	bash \
"

RDEPENDS_${PN}_append_x64 = "fw-printenv \
"

group = "${LVRT_GROUP}"

S = "${WORKDIR}"

do_install () {
	install -d ${D}${bindir}
	install -d ${D}${sysconfdir}
	install -d ${D}${libdir}
	install -m 0755   ${WORKDIR}/status_led         ${D}${bindir}
	install -m 0440   ${WORKDIR}/nisetbootmode.functions         ${D}${libdir}
	install -m 0550   ${WORKDIR}/nisetbootmode         ${D}${bindir}
	install -m 0755   ${WORKDIR}/ninetcfgutil       ${D}${bindir}
	install -m 0444   ${WORKDIR}/ninetcfgutil_platdep.sh ${D}${libdir}

	if [ "${TARGET_ARCH}" = "arm" ]; then
		install -m 0644   ${WORKDIR}/fw_env.config         ${D}${sysconfdir}
	fi

	chown 0:${group} ${D}${bindir}/status_led

}
