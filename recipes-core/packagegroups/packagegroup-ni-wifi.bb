# (C) Copyright 2013,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "Wi-Fi packages for NI Linux Realtime distribution"
LICENSE = "MIT"
PR = "r1"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} = "\
	crda \
	iw \
	libnl \
	openssl \
	rfkill \
	ti-wifi-utils \
	wpa-supplicant \
	wireless-regdb"
