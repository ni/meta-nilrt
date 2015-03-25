# (C) Copyright 2013,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "Runmode specific packages for NI Linux Realtime distribution"
LICENSE = "MIT"
PR = "r1"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} = "\
	base-files-runmode \
	cronie \
	dosfstools \
	e2fsprogs-e2fsck \
	e2fsprogs-tune2fs \
	gdbserver \
	glibc-gconv-cp932 \
	glibc-gconv-cp936 \
	glibc-gconv-iso8859-1 \
	initscripts-runmode \
	logrotate \
	ni-module-versioning-tools \
	parted \
	util-linux-sfdisk \
	udev-cache \
	zip \
	${@base_contains("DISTRO_FEATURES", "x11", "sysconfig-settings-ui", "", d)} \
"
