DESCRIPTION = "The calibrator and other useful utilities for TI wireless solution based on wl12xx driver"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://COPYING;md5=4725015cb0be7be389cf06deeae3683d"

DEPENDS = "libnl"

PV = "R8.6+git${SRCPV}"

#Tag: R8.6
SRCREV = "cf8965aad73764022669647fa33852558a657930"
SRC_URI = "git://git.ti.com/wilink8-wlan/18xx-ti-utils.git \
"

S = "${WORKDIR}/git"

export CROSS_COMPILE = "${TARGET_PREFIX}"

EXTRA_OEMAKE = 'CFLAGS="${CFLAGS} -I${STAGING_INCDIR}/libnl3/ -DCONFIG_LIBNL32 " \
		LDFLAGS="${LDFLAGS} -L${STAGING_LIBDIR}" \
		CC="${CC}" \
		NLVER=3'

do_install() {
    install -d ${D}${bindir}
    install -m 0755 calibrator ${D}${bindir}/
}
