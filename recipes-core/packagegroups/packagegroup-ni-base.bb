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
	linux-firmware-radeon \
	dmidecode \
	efivar \
"

NILRT_NXG_ARM_PACKAGES = "\
	u-boot-fw-utils \
	kernel-devicetree \
"

NILRT_NXG_x64_PACKAGES = "\
	grub-efi-nilrt \
"

NILRT_ARM_PACKAGES = "\
	mtd-utils-ubifs \
"

NILRT_x64_PACKAGES = "\
	e2fsprogs \
	e2fsprogs-mke2fs \
	phc2sys \
	nilrtdiskcrypt \
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
	python-pyiface \
	python-configparser \
	${@bb.utils.contains('TARGET_ARCH', 'arm', \
		'${NILRT_NXG_ARM_PACKAGES}', \
		'${NILRT_NXG_x64_PACKAGES}', d)} \
"

NILRT_PACKAGES = "\
	busybox-ifplugd \
	glibc-gconv-utf-16 \
	gptfdisk-sgdisk \
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
	ethtool \
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
	run-postinsts \
	start-stop-daemon \
	sudo \
	syslog-ng \
	sysvinit \
	tar \
	eudev \
	udev-extraconf \
	util-linux-agetty \
	util-linux-hwclock \
	util-linux-mount \
	util-linux-umount \
	util-linux-runuser \
	${VIRTUAL-RUNTIME_mountpoint} \
	${MACHINE_ESSENTIAL_EXTRA_RDEPENDS} \
	${@bb.utils.contains('TARGET_ARCH', 'arm', \
		'${ALL_DISTRO_ARM_PACKAGES}', \
		'${ALL_DISTRO_x64_PACKAGES}', d)} \
	${@base_conditional('DISTRO', 'nilrt-nxg', \
		'${NILRT_NXG_PACKAGES}', \
		'${NILRT_PACKAGES}', d)} \
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
	kernel-module-i40e \
	kernel-module-i8042 \
	kernel-module-i915 \
	kernel-module-igb \
	kernel-module-intel_agp \
	kernel-module-ipv6 \
	kernel-module-nic7018-wdt \
	kernel-module-leds-nic78bx \
	kernel-module-ixgbe \
	kernel-module-psmouse \
	kernel-module-squashfs \
	kernel-module-tg3 \
	kernel-module-tpm-tis \
	kernel-module-tulip \
	kernel-module-usbtouchscreen \
	kernel-module-wacom \
	kernel-module-virtio-balloon \
	kernel-module-virtio-blk \
	kernel-module-virtio-console \
	kernel-module-virtio-net \
"

RRECOMMENDS_${PN}_xilinx_zynqhf += "\
	kernel-module-g-ether \
	kernel-module-u-ether \
	kernel-module-ci-hdrc \
	kernel-module-ci-hdrc-usb2 \
	kernel-module-udc-core \
	kernel-module-wl12xx \
	kernel-module-wlcore \
	kernel-module-mac80211 \
	kernel-module-cfg80211 \
"
