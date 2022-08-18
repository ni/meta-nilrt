# (C) Copyright 2014,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "SELinux packages for the NI Linux Real-Time distribution"
LICENSE = "MIT"


PACKAGE_ARCH = "${MACHINE_ARCH}"


inherit packagegroup


RDEPENDS:${PN} = "\
	coreutils-chcon \
	coreutils-ls \
	packagegroup-core-selinux \
"
