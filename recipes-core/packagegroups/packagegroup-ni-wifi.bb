# (C) Copyright 2013,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "Wi-Fi packages for NI Linux Realtime distribution"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} = "\
	iw \
	libnl \
	ni-wireless-ath6kl-fw \
	openssl \
	rfkill \
	wpa-supplicant \
	wireless-regdb-static \
"

RDEPENDS:${PN}:append:xilinx-zynq = " \
	ni-wireless-wl127x-fw \
	ti-wifi-utils \
"
