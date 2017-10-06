# (C) Copyright 2016,
# National Instruments Corporation.
# All rights reserved.

SUMMARY = "Ptest packages for the NI Linux Realtime distribution"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} = "\
	ptest-runner \
	rt-tests-ptest \
	kernel-tests-ptest \
	glibc-tests-ptest \
	salt-ptest \
	opkg-ptest \
	distro-feed-configs-ptest \
"
