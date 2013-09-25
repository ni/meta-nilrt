# (C) Copyright 2013,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "Base set of packages for NI Linux Realtime distribution"
LICENSE = "MIT"
PR = "r1"

inherit packagegroup

PACKAGE_ARCH = "${MACHINE_ARCH}"

#
# Set by the machine configuration with packages essential for device bootup
#
MACHINE_ESSENTIAL_EXTRA_RDEPENDS ?= ""
MACHINE_ESSENTIAL_EXTRA_RRECOMMENDS ?= ""

RDEPENDS_${PN} = "\
	avahi-daemon \
	avahi-dnsconfd \
	base-files \
	base-passwd \
	${@base_contains("MACHINE_FEATURES", "keyboard", "keymaps", "", d)} \
	busybox \
	busybox-ifplugd \
	busybox-mdev \
	coreutils-hostname \
	dhcp-client \
	e2fsprogs \
	e2fsprogs-mke2fs \
	ethtool \
	glibc-gconv-utf-16 \
	init-ifupdown \
	initscripts \
	iproute2 \
	iptables \
	libavahi-client \
	libavahi-common \
	libavahi-core \
	libcap-bin \
	libnss-mdns \
	libpam \
	libstdc++ \
	mtd-utils \
	mtd-utils-ubifs \
	netbase \
	openssh-sshd \
	openssh-scp \
	openssh-sftp-server \
	openssh-sftp \
	openssh-ssh \
	openvpn \
	opkg \
	pigz \
	syslog-ng \
	sysvinit \
	tar \
	util-linux-findfs \
	util-linux-hwclock \
	util-linux-mount \
	util-linux-umount \
	${MACHINE_ESSENTIAL_EXTRA_RDEPENDS}"

RRECOMMENDS_${PN} = "\
	${MACHINE_ESSENTIAL_EXTRA_RRECOMMENDS}"
