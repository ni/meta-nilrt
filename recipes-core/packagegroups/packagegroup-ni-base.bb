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
	grub-efi-rootfs-chainloaded \
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
	gptfdisk-sgdisk \
	init-ifupdown \
	libstdc++ \
	logrotate \
	niwatchdogpet \
	ni-module-versioning-tools \
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
	${@oe.utils.conditional('DISTRO', 'nilrt-nxg', \
		'${NILRT_NXG_PACKAGES}', \
		'${NILRT_PACKAGES}', d)} \
"

RRECOMMENDS_${PN}_x64 += "\
	kernel-module-ablk-helper \
	kernel-module-aesni-intel \
	kernel-module-agpart \
	kernel-module-ath6kl-core \
	kernel-module-ath6kl-sdio \
	kernel-module-atkbd \
	kernel-module-backlight \
	kernel-module-button \
	kernel-module-cfg80211 \
	kernel-module-coretemp \
	kernel-module-crypto-simd \
	kernel-module-drm \
	kernel-module-drm-kms-helper \
	kernel-module-dwc3 \
	kernel-module-dwc3-pci \
	kernel-module-e100 \
	kernel-module-e1000 \
	kernel-module-e1000e \
	kernel-module-fb-sys-fops \
	kernel-module-g-ether \
	kernel-module-glue-helper \
	kernel-module-u-ether \
	kernel-module-hid-microsoft \
	kernel-module-i2c-algo-bit \
	kernel-module-i2c-i801 \
	kernel-module-i2c-smbus \
	kernel-module-i40e \
	kernel-module-i8042 \
	kernel-module-i915 \
	kernel-module-igb \
	kernel-module-intel_agp \
	kernel-module-ipv6 \
	kernel-module-mfd-core \
	kernel-module-nic7018-wdt \
	kernel-module-leds-nic78bx \
	kernel-module-libcomposite \
	kernel-module-lpc-ich \
	kernel-module-ixgbe \
	kernel-module-psmouse \
	kernel-module-phy-generic \
	kernel-module-radeon \
	kernel-module-squashfs \
	kernel-module-syscopyarea \
	kernel-module-sysfillrect \
	kernel-module-sysimgblt \
	kernel-module-tg3 \
	kernel-module-tpm \
	kernel-module-tpm-tis \
	kernel-module-tpm-tis-core \
	kernel-module-ttm \
	kernel-module-tulip \
	kernel-module-udc-core \
	kernel-module-usbtouchscreen \
	kernel-module-usb-f-eem \
	kernel-module-usb-storage \
	kernel-module-wacom \
	kernel-module-video \
	kernel-module-virtio-balloon \
	kernel-module-virtio-blk \
	kernel-module-virtio-console \
	kernel-module-virtio-net \
"

RRECOMMENDS_${PN}_armv7a += "\
	kernel-module-g-ether \
	kernel-module-u-ether \
	kernel-module-ci-hdrc \
	kernel-module-ci-hdrc-usb2 \
	kernel-module-udc-core \
	kernel-module-wl12xx \
	kernel-module-wlcore \
	kernel-module-mac80211 \
	kernel-module-cfg80211 \
	kernel-module-squashfs \
	kernel-module-fuse \
	kernel-module-ipv6 \
	kernel-module-libcomposite \
	kernel-module-phy-generic \
	kernel-module-usb-f-eem \
	kernel-module-usb-storage \
"
