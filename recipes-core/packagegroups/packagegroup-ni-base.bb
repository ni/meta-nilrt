# (C) Copyright 2013,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "Base set of packages for NI Linux Realtime distribution"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

#
# Set by the machine configuration with packages essential for device bootup
#
MACHINE_ESSENTIAL_EXTRA_RDEPENDS ?= ""
MACHINE_ESSENTIAL_EXTRA_RRECOMMENDS ?= ""

RDEPENDS_${PN} = "\
	avahi-daemon \
	base-files \
	base-passwd \
	${@base_contains("MACHINE_FEATURES", "keyboard", "keymaps", "", d)} \
	busybox \
	busybox-ifplugd \
	${@base_contains("MACHINE_FEATURES", "acpi", "busybox-acpid", "", d)} \
	coreutils-hostname \
	cronie \
	dhcp-client \
	dmidecode \
	e2fsprogs \
	e2fsprogs-mke2fs \
	ethtool \
	fuse-exfat \
	glibc-gconv-utf-16 \
	gptfdisk-sgdisk \
	init-ifupdown \
	initscripts \
	iproute2 \
	iproute2-tc \
	iptables \
	kmod \
	libavahi-client \
	libavahi-common \
	libavahi-core \
	libcap-bin \
	libnss-mdns \
	libpam \
	libstdc++ \
	logrotate \
	lsbinitscripts \
	mtd-utils \
	mtd-utils-ubifs \
	netbase \
	niacctbase \
	niwatchdogpet \
	openssh-sshd \
	openssh-scp \
	openssh-sftp-server \
	openssh-sftp \
	openssh-ssh \
	openvpn \
	opkg \
	${@base_contains('COMBINED_FEATURES', 'pci', 'pciutils-ids', '',d)} \
	pigz \
	start-stop-daemon \
	syslog-ng \
	sysvinit \
	tar \
	udev-extraconf \
	usbutils \
	util-linux-agetty \
	util-linux-findfs \
	util-linux-hwclock \
	util-linux-mount \
	util-linux-umount \
	vlan \
	${VIRTUAL-RUNTIME_mountpoint} \
	${VIRTUAL-RUNTIME_dev_manager} \
	${MACHINE_ESSENTIAL_EXTRA_RDEPENDS}"

RDEPENDS_${PN}_append_x64= "\
	linux-firmware-i915 \
	phc2sys"

RRECOMMENDS_${PN} = "\
	${MACHINE_ESSENTIAL_EXTRA_RRECOMMENDS}"
