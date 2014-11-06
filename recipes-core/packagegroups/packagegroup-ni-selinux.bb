# (C) Copyright 2014,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "SELinux packages for the NI Linux Real-Time distribution"
LICENSE = "MIT"
PR = "r1"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} = "\
	packagegroup-core-selinux \
	coreutils-ls \
	coreutils-chcon \
	"
