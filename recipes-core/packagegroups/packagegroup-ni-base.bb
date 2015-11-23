# (C) Copyright 2013,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "Base set of packages for NI Linux Realtime distribution"
LICENSE = "MIT"
PR = "r1"

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
	base-files-nilrt \
	base-passwd \
	${@base_contains("MACHINE_FEATURES", "keyboard", "keymaps", "", d)} \
	busybox \
	busybox-ifplugd \
	${@base_contains("MACHINE_FEATURES", "acpi", "busybox-acpid", "", d)} \
	coreutils-hostname \
	dhcp-client \
	distro-feed-configs \
	dmidecode \
	ethtool \
	init-ifupdown \
	initscripts \
	initscripts-nilrt \
	iproute2 \
	iptables \
	kmod \
	libavahi-client \
	libavahi-common \
	libavahi-core \
	libnss-mdns \
	libpam \
	lsb \
	lsbinitscripts \
	modutils-initscripts \
	ni-utils \
	netbase \
	niacctbase \
	openssh-sshd \
	openssh-scp \
	openssh-sftp-server \
	openssh-ssh \
	opkg \
	opkg-utils \
	python-futures \
	rtctl \
	salt-minion \
	start-stop-daemon \
	syslog-ng \
	sysvinit \
	tar \
	udev-extraconf \
	util-linux-agetty \
	util-linux-hwclock \
	util-linux-mount \
	util-linux-umount \
	util-linux-runuser \
	${VIRTUAL-RUNTIME_mountpoint} \
	${VIRTUAL-RUNTIME_dev_manager} \
	${MACHINE_ESSENTIAL_EXTRA_RDEPENDS} \
"

RDEPENDS_${PN}_append_x64 += "\
	efivar \
	grub-efi-nilrt \
"

RDEPENDS_${PN}_append_xilinx-zynq += "\
	mtd-utils \
"

RRECOMMENDS_${PN} = "\
	${MACHINE_ESSENTIAL_EXTRA_RRECOMMENDS}"
