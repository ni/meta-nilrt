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

ALL_DISTRO_ARM_PACKAGES = "\
	mtd-utils \
	u-boot-fw-utils \
"

ALL_DISTRO_x64_PACKAGES = "\
	linux-firmware-i915 \
	linux-firmware-radeon \
	dmidecode \
	efivar \
	fw-printenv \
"

NILRT_NXG_ARM_PACKAGES = "\
	kernel-devicetree \
"

NILRT_NXG_x64_PACKAGES = "\
	efibootmgr \
"

NILRT_ARM_PACKAGES = "\
	jitterentropy-rngd \
	mtd-utils-ubifs \
"

NILRT_x64_PACKAGES = "\
	e2fsprogs \
	e2fsprogs-mke2fs \
	phc2sys \
	nilrtdiskcrypt \
"

NILRT_NXG_PACKAGES = "\
	modutils-initscripts \
	ni-utils \
	rauc \
	rauc-mark-good \
	packagegroup-ni-minimal-transconf \
	rtctl \
	salt-minion \
	connman \
	${@bb.utils.contains('TARGET_ARCH', 'arm', \
		'${NILRT_NXG_ARM_PACKAGES}', \
		'${NILRT_NXG_x64_PACKAGES}', d)} \
"

NILRT_PACKAGES = "\
	busybox-ifplugd \
	glibc-gconv-utf-16 \
	init-ifupdown \
	libstdc++ \
	logrotate \
	niwatchdogpet \
	openvpn \
	pigz \
	usbutils \
	${@bb.utils.contains('COMBINED_FEATURES', 'pci', 'pciutils-ids', '',d)} \
	${@bb.utils.contains('TARGET_ARCH', 'arm', \
		'${NILRT_ARM_PACKAGES}', \
		'${NILRT_x64_PACKAGES}', d)} \
"

RDEPENDS_${PN} = "\
	avahi-daemon \
	base-files \
	base-files-nilrt \
	base-passwd \
	${@bb.utils.contains('MACHINE_FEATURES', 'keyboard', 'keymaps', '', d)} \
	busybox \
	${@bb.utils.contains('MACHINE_FEATURES', 'acpi', 'busybox-acpid', '', d)} \
	coreutils-hostname \
	cronie \
	curl \
	daemonize \
	dhcp-client \
	dpkg-start-stop \
	ethtool \
	gptfdisk-sgdisk \
	initscripts \
	initscripts-nilrt \
	iproute2 \
	iptables \
	kmod \
	kernel-modules \
	libavahi-client \
	libavahi-common \
	libavahi-core \
	libcap-bin\
	libnss-mdns \
	libpam \
	librtpi \
	lsbinitscripts \
	netbase \
	niacctbase \
	openssh-sshd \
	openssh-scp \
	openssh-sftp-server \
	openssh-ssh \
	opkg \
	opkg-keyrings \
	os-release \
	run-postinsts \
	sudo \
	syslog-ng \
	sysvinit \
	tar \
	eudev \
	udev-extraconf \
	util-linux-agetty \
	hwclock-init \
	util-linux-hwclock \
	util-linux-mount \
	util-linux-umount \
	util-linux-runuser \
	${VIRTUAL-RUNTIME_mountpoint} \
	${MACHINE_ESSENTIAL_EXTRA_RDEPENDS} \
	${@bb.utils.contains('TARGET_ARCH', 'arm', \
		'${ALL_DISTRO_ARM_PACKAGES}', \
		'${ALL_DISTRO_x64_PACKAGES}', d)} \
	${@oe.utils.conditional('DISTRO', 'nilrt-nxg', \
		'${NILRT_NXG_PACKAGES}', \
		'${NILRT_PACKAGES}', d)} \
"
