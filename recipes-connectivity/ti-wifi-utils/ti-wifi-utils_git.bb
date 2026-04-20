DESCRIPTION = "The calibrator and other useful utilities for TI wireless solution based on wl12xx driver"
LICENSE = "0BSD"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/0BSD;md5=f667a3c3830a55a17ec3067709f4526c"

DEPENDS = "libnl"

PV = "R8.6+git${SRCPV}"

#Tag: R8.6
SRCREV = "cf8965aad73764022669647fa33852558a657930"
SRC_URI = "git://git.ti.com/wilink8-wlan/18xx-ti-utils.git;protocol=https;branch=master \
           file://0001-fix-packed-member-warning.patch \
           file://0001-fix-efuse_param_type_enm.patch \
"

export CROSS_COMPILE = "${TARGET_PREFIX}"

EXTRA_OEMAKE = 'CFLAGS="${CFLAGS} -I${STAGING_INCDIR}/libnl3/ -DCONFIG_LIBNL32 " \
		LDFLAGS="${LDFLAGS} -L${STAGING_LIBDIR}" \
		CC="${CC}" \
		NLVER=3'

do_install() {
    install -d ${D}${bindir}
    install -m 0755 calibrator ${D}${bindir}/
}
