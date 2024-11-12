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


RDEPENDS:${PN} = "\
	${MACHINE_ESSENTIAL_EXTRA_RDEPENDS} \
	${VIRTUAL-RUNTIME_mountpoint} \
"

RDEPENDS:${PN} += "\
	${@bb.utils.contains('COMBINED_FEATURES', 'pci', 'pciutils-ids', '',d)} \
	${@bb.utils.contains('MACHINE_FEATURES', 'acpi', 'busybox-acpid', '', d)} \
	${@bb.utils.contains('MACHINE_FEATURES', 'keyboard', 'keymaps', '', d)} \
	avahi-daemon \
	base-files \
	base-files-nilrt \
	base-passwd \
	busybox \
	busybox-ifplugd \
	busybox-udhcpd \
	busybox-zcip \
	coreutils-hostname \
	crio-support-scripts \
	cronie \
	curl \
	daemonize \
	dmidecode \
	dpkg-start-stop \
	e2fsprogs \
	e2fsprogs-mke2fs \
	efibootmgr \
	efivar \
	ethtool \
	eudev \
	fw-printenv \
	glibc-gconv-utf-16 \
	gptfdisk \
	init-ifupdown \
	initscripts \
	initscripts-nilrt \
	iproute2 \
	iptables \
	kernel-modules \
	kmod \
	libavahi-client \
	libavahi-common \
	libavahi-core \
	libcap-bin\
	libnss-mdns \
	libpam \
	librtpi \
	libstdc++ \
	linux-firmware-i915 \
	logrotate \
	lsbinitscripts \
	modutils-initscripts \
	netbase \
	ni-hw-scripts \
	ni-rtfeatures \
	ni-safemode-utils \
	ni-shutdown-guard \
	ni-systemformat \
	ni-utils \
	niacctbase \
	niwatchdogpet \
	openssh-scp \
	openssh-sftp-server \
	openssh-ssh \
	openssh-sshd \
	openvpn \
	opkg \
	opkg-keyrings \
	os-release \
	pigz \
	pstore-save \
	run-postinsts \
	sudo \
	sysconfig-settings \
	sysconfig-settings-console \
	syslog-ng \
	sysvinit \
	tar \
	udev-extraconf \
	usbutils \
	util-linux-agetty \
	util-linux-hwclock \
	util-linux-mount \
	util-linux-runuser \
	util-linux-umount \
"
