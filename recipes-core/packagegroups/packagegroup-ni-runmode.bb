# (C) Copyright 2013,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "Runmode specific packages for NI Linux Realtime distribution"
LICENSE = "MIT"
PR = "r1"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} = "\
	ca-certificates \
	cronie \
	curl \
	dosfstools \
	e2fsprogs \
	e2fsprogs-e2fsck \
	e2fsprogs-mke2fs \
	e2fsprogs-tune2fs \
	gdbserver \
	glibc-gconv-cp932 \
	glibc-gconv-cp936 \
	glibc-gconv-iso8859-1 \
	glibc-gconv-utf-16 \
	gptfdisk-sgdisk \
	libcap-bin \
	libstdc++ \
	logrotate \
	niwatchdogpet \
	ni-module-versioning-tools \
	openssh-sftp-server \
	openssh-sftp \
	openssl \
	openvpn \
	parted \
	${@base_contains('COMBINED_FEATURES', 'pci', 'pciutils-ids', '',d)} \
	pigz \
	sysconfig-settings \
	usbutils-ids \
	util-linux-findfs \
	util-linux-sfdisk \
	udev-cache \
	vlan \
	zip \
	${@base_contains("DISTRO_FEATURES", "x11", "sysconfig-settings-ui", "", d)} \
"
