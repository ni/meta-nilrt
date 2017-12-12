DESCRIPTION = "The calibrator and other useful utilities for TI wireless solution based on wl12xx driver"
LICENSE = "CLOSED"

DEPENDS = "libnl"

PV ="0.0-git${SRCPV}"

SRCREV = "f4508cf40df603456d806e2c64fd4a99b13d1aaf"
SRC_URI = "git://github.com/TI-OpenLink/ti-utils.git;protocol=git \
	file://upgrade_libnl_to_32.patch"

S = "${WORKDIR}/git"

export CROSS_COMPILE = "${TARGET_PREFIX}"
CFLAGS += " -DCONFIG_LIBNL20 -I${STAGING_INCDIR}/libnl3"

do_install() {
	install -d ${D}${bindir}

	install -m 0755 calibrator ${D}${bindir}/
}
