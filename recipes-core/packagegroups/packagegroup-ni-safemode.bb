# (C) Copyright 2019,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "Safemode specific packages for NI Linux Realtime distribution"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} = " \
	initscripts-nilrt-safemode \
"
