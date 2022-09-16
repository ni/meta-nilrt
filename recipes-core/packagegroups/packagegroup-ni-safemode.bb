# (C) Copyright 2019,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "Safemode specific packages for NI Linux Realtime distribution"
LICENSE = "MIT"


PACKAGE_ARCH = "${MACHINE_ARCH}"


inherit packagegroup


RDEPENDS:${PN} = " \
	initscripts-nilrt-safemode \
	e2fsprogs \
	e2fsprogs-e2fsck \
	e2fsprogs-mke2fs \
	e2fsprogs-tune2fs \
	ni-netcfgutil \
	ni-shutdown-guard-safemode \
	ni-systemimage \
	sysconfig-settings-ssh \
"

RDEPENDS:${PN}:append:armv7a = " \
	nisdbootconfig \
"
