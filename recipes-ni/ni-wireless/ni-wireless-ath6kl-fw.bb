# Copyright (C) 2026 National Instruments

SUMMARY = "NI wireless firmware for ath6k"
HOMEPAGE = "http://www.ni.com"
LICENSE = "CLOSED"
SECTION = "base"

SRC_URI = "\
	file://ath6kl_core.conf \
	file://firmware/ath6k/AR6004/hw3.0/bdata.00.bin \
	file://firmware/ath6k/AR6004/hw3.0/bdata.02.bin \
	file://firmware/ath6k/AR6004/hw3.0/bdata.10.bin \
	file://firmware/ath6k/AR6004/hw3.0/bdata.12.bin \
	file://firmware/ath6k/AR6004/hw3.0/bdata.13.bin \
	file://firmware/ath6k/AR6004/hw3.0/bdata.US.bin \
	file://firmware/ath6k/AR6004/hw3.0/fw-5.bin \
"

inherit allarch

FILES:${PN} += "\
	${sysconfdir}/modprobe.d/ath6kl_core.conf \
	${base_libdir}/firmware/ath6k/AR6004/hw3.0/*.bin \
"

RDEPENDS:${PN} += "ni-wireless-common"

S = "${WORKDIR}"

do_install() {
	install -pd ${D}${base_libdir}/firmware/ath6k/AR6004/hw3.0
	install -m 0644 ${S}/firmware/ath6k/AR6004/hw3.0/*.bin ${D}${base_libdir}/firmware/ath6k/AR6004/hw3.0

	install -dm 0755 ${D}${sysconfdir}/modprobe.d
	install -m 0644 ${S}/ath6kl_core.conf ${D}${sysconfdir}/modprobe.d
}
