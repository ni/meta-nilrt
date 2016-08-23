# (C) Copyright 2016,
# National Instruments Corporation.
# All rights reserved.

SUMMARY = "Ptest packages for the NI Linux Realtime distribution"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} = "\
	ptest-runner \
	rt-tests \
	kernel-tests-ptest \
	glibc-tests-ptest \
	salt-ptest \
"
