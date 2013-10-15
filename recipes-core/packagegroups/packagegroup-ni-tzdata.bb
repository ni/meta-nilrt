# (C) Copyright 2013,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "Time zone packages for NI Linux Realtime distribution"
LICENSE = "MIT"
PR = "r1"

inherit packagegroup

PACKAGE_ARCH = "${MACHINE_ARCH}"

RDEPENDS_${PN} = "\
	tzdata \
	tzdata-africa \
	tzdata-americas \
	tzdata-asia \
	tzdata-atlantic \
	tzdata-australia \
	tzdata-europe \
	tzdata-misc \
	tzdata-pacific"
