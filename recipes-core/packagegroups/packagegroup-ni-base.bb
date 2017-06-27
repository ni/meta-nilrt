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
"

ALL_DISTRO_x64_PACKAGES = "\
	linux-firmware-i915 \
	dmidecode \
"

NILRT_NXG_ARM_PACKAGES = "\
	u-boot-fw-utils \
	kernel-devicetree \
"

NILRT_NXG_x64_PACKAGES = "\
	efivar \
	grub-efi-nilrt \
"

NILRT_ARM_PACKAGES = "\
	mtd-utils-ubifs \
"

NILRT_x64_PACKAGES = "\
	e2fsprogs \
	e2fsprogs-mke2fs \
	phc2sys \
"

NILRT_NXG_PACKAGES = "\
	distro-feed-configs \
	lsb \
	modutils-initscripts \
	ni-utils \
	python-futures \
	rtctl \
	salt-minion \
	connman \
	${@base_contains('TARGET_ARCH', 'arm', \
		'${NILRT_NXG_ARM_PACKAGES}', \
		'${NILRT_NXG_x64_PACKAGES}', d)} \
"

NILRT_PACKAGES = "\
	cronie \
	glibc-gconv-utf-16 \
	gptfdisk-sgdisk \
	init-ifupdown \
	libstdc++ \
	logrotate \
	niwatchdogpet \
	pigz \
	usbutils \
	${@base_contains('COMBINED_FEATURES', 'pci', 'pciutils-ids', '',d)} \
	${@base_contains('TARGET_ARCH', 'arm', \
		'${NILRT_ARM_PACKAGES}', \
		'${NILRT_x64_PACKAGES}', d)} \
"

RDEPENDS_${PN} = "\
	avahi-daemon \
	base-files \
	base-files-nilrt \
	base-passwd \
	${@base_contains('MACHINE_FEATURES', 'keyboard', 'keymaps', '', d)} \
	busybox \
	${@base_contains('MACHINE_FEATURES', 'acpi', 'busybox-acpid', '', d)} \
	coreutils-hostname \
	dhcp-client \
	ethtool \
	fuse-exfat \
	initscripts \
	initscripts-nilrt \
	iproute2 \
	iptables \
	kmod \
	libavahi-client \
	libavahi-common \
	libavahi-core \
	libcap-bin\
	libnss-mdns \
	libpam \
	lsbinitscripts \
	netbase \
	niacctbase \
	openssh-sshd \
	openssh-scp \
	openssh-sftp-server \
	openssh-ssh \
	opkg \
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
	${@base_contains('TARGET_ARCH', 'arm', \
		'${ALL_DISTRO_ARM_PACKAGES}', \
		'${ALL_DISTRO_x64_PACKAGES}', d)} \
	${@base_conditional('DISTRO', 'nilrt-nxg', \
		'${NILRT_NXG_PACKAGES}', \
		'${NILRT_PACKAGES}', d)} \
"

# these packages are needed only on the n310 machine (not even all ARMs)
RDEPENDS_${PN}_append_n310 = "\
	e2fsprogs-resize2fs \
"

# for older NILRT these will automatically be removed because of the
# PACKAGE_REMOVE = kernel-* rule in niconsole-image.inc
# TODO: We need to clean up this list and remove non-essentials, move
# them to individual recipes or to linux-nilrt as runtime dependencies
RRECOMMENDS_${PN}_x64 += "\
	kernel-module-atkbd \
	kernel-module-coretemp \
	kernel-module-e100 \
	kernel-module-e1000 \
	kernel-module-e1000e \
	kernel-module-hid-microsoft \
	kernel-module-i915 \
	kernel-module-igb \
	kernel-module-intel_agp \
	kernel-module-ipv6 \
	kernel-module-psmouse \
	kernel-module-squashfs \
	kernel-module-tg3 \
	kernel-module-usbtouchscreen \
	kernel-module-wacom \
	kernel-module-virtio-balloon \
	kernel-module-virtio-blk \
	kernel-module-virtio-console \
	kernel-module-virtio-net \
"
