# (C) Copyright 2013,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "Wi-Fi packages for NI Linux Realtime distribution"
LICENSE = "MIT"


PACKAGE_ARCH = "${MACHINE_ARCH}"


inherit packagegroup


RDEPENDS:${PN} = "\
	crda \
	iw \
	libnl \
	openssl \
	rfkill \
	wireless-regdb \
	wpa-supplicant \
"
