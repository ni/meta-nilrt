# Copyright (C) 2026 National Instruments

SUMMARY = "NI wireless firmware for wl127x"
HOMEPAGE = "http://www.ni.com"
SECTION = "base"

LICENSE = "TI-TSPA"
NO_GENERIC_LICENSE[TI-TSPA] = "LICENCE.wl127x"
LIC_FILES_CHKSUM = "file://LICENCE.wl127x;md5=ba590e1d103f891d0151609046aef9e8"

SRC_URI = "\
	file://niwl12xxmakenvs \
	file://LICENCE.wl127x \
	file://firmware/ti-connectivity/wl127x-fw-4-mr.bin \
	file://firmware/ti-connectivity/wl127x-fw-4-plt.bin \
	file://firmware/ti-connectivity/wl127x-fw-4-sr.bin \
"

inherit allarch update-rc.d

FILES:${PN} += "\
	${base_libdir}/firmware/ti-connectivity/*.bin \
"

RDEPENDS:${PN} += "ni-wireless-common"

S = "${WORKDIR}"

INITSCRIPT_NAME = "niwl12xxmakenvs"
INITSCRIPT_PARAMS = "start 04 S ."

do_install() {
	install -d ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/niwl12xxmakenvs ${D}${sysconfdir}/init.d/

	install -pd ${D}${base_libdir}/firmware/ti-connectivity
	# Rename our tested and known-working firmware files so they work on newer kernels
	install -m 0644 ${S}/firmware/ti-connectivity/wl127x-fw-4-mr.bin ${D}${base_libdir}/firmware/ti-connectivity/wl127x-fw-5-mr.bin
	install -m 0644 ${S}/firmware/ti-connectivity/wl127x-fw-4-plt.bin ${D}${base_libdir}/firmware/ti-connectivity/wl127x-fw-5-plt.bin
	install -m 0644 ${S}/firmware/ti-connectivity/wl127x-fw-4-sr.bin ${D}${base_libdir}/firmware/ti-connectivity/wl127x-fw-5-sr.bin
}
