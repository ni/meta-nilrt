SUMMARY = "initramfs specific packages for NI Linux Realtime distribution"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += "\
	base-passwd \
	bash \
	busybox \
	bzip2 \
	coreutils \
	dosfstools \
	e2fsprogs \
	e2fsprogs-mke2fs \
	e2fsprogs-tune2fs \
	findutils \
	fw-printenv         \
	gawk \
	gptfdisk \
	grep \
	initscripts \
	init-ifupdown \
	init-restore-mode \
	iproute2 \
	kmod \
	ni-hw-scripts \
	ni-utils \
	ni-systemreplication \
	openssh-keygen \
	openssh-sshd \
	parted \
	procps \
	sed \
	sysvinit \
	tar \
	util-linux \
	util-linux-agetty \
	vim-tiny \
"

RDEPENDS:${PN}:append:x64 = "\
	dmidecode           \
	efibootmgr          \
	efivar              \
	eudev               \
	busybox-ifplugd     \
	busybox-zcip        \
	grub                \
	grub-editenv        \
	grub-efi            \
	packagegroup-kernel-modules-essential \
	ni-smbios-helper    \
	udev-extraconf      \
	"

RDEPENDS:${PN}:append:xilinx-zynq = "\
	mtd-utils           \
	mtd-utils-ubifs     \
	"

