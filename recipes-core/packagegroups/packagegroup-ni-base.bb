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

ALL_DISTRO_x64_PACKAGES = "\
	linux-firmware-i915 \
	dmidecode \
	efivar \
	fw-printenv \
"

NILRT_NXG_x64_PACKAGES = "\
	efibootmgr \
"

NILRT_x64_PACKAGES = "\
	e2fsprogs \
	e2fsprogs-mke2fs \
	phc2sys \
	pstore-save \
	nilrtdiskcrypt \
"

NILRT_NXG_PACKAGES = "\
	modutils-initscripts \
	packagegroup-ni-minimal-transconf \
	rtctl \
	salt-minion \
	connman \
	${NILRT_NXG_x64_PACKAGES} \
"

NILRT_PACKAGES = "\
	busybox-ifplugd \
	busybox-udhcpd \
	busybox-zcip \
	glibc-gconv-utf-16 \
	init-ifupdown \
	libstdc++ \
	logrotate \
	niwatchdogpet \
	openvpn \
	pigz \
	usbutils \
	${@bb.utils.contains('COMBINED_FEATURES', 'pci', 'pciutils-ids', '',d)} \
	${NILRT_x64_PACKAGES} \
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
	crio-support-scripts \
	cronie \
	curl \
	daemonize \
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
	ni-hw-scripts \
	ni-utils \
	ni-safemode-utils \
	ni-shutdown-guard \
	ni-systemformat \
	openssh-sshd \
	openssh-scp \
	openssh-sftp-server \
	openssh-ssh \
	opkg \
	opkg-keyrings \
	os-release \
	run-postinsts \
	sudo \
	sysconfig-settings \
	sysconfig-settings-ui \
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
	${ALL_DISTRO_x64_PACKAGES} \
	${@oe.utils.conditional('DISTRO', 'nilrt-nxg', \
		'${NILRT_NXG_PACKAGES}', \
		'${NILRT_PACKAGES}', d)} \
"
