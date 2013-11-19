# (C) Copyright 2013,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "Runmode specific packages for NI Linux Realtime distribution"
LICENSE = "MIT"
PR = "r1"

inherit packagegroup

PACKAGE_ARCH = "${MACHINE_ARCH}"

RDEPENDS_${PN} = "\
	busybox-cron \
	dosfstools \
	e2fsprogs-e2fsck \
	e2fsprogs-tune2fs \
	gdbserver \
	glibc-gconv-cp932 \
	glibc-gconv-iso8859-1 \
	logrotate \
	parted \
	util-linux-sfdisk"

RDEPENDS_${PN}_append_x86-64 = " glibc-gconv-cp936"

