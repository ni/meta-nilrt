SUMMARY = "initramfs specific packages for NI Linux Realtime distribution"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += "\
	${@bb.utils.contains('INIT_MANAGER', 'systemd', 'systemd', 'sysvinit', d)} \
	${@bb.utils.contains('INIT_MANAGER', 'sysvinit', 'eudev', '', d)} \
	base-passwd \
	bash \
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
	init-restore-mode \
	kmod \
	ni-systemreplication \
	parted \
	procps \
	sed \
	tar \
	util-linux \
	util-linux-agetty \
	vim-tiny \
"

RDEPENDS:${PN}:append:x64 = "\
	dmidecode           \
	efibootmgr          \
	efivar              \
	grub                \
	grub-editenv        \
	grub-efi            \
	ni-smbios-helper    \
	"

RDEPENDS:${PN}:append:xilinx-zynq = "\
	mtd-utils           \
	mtd-utils-ubifs     \
	"

RRECOMMENDS:${PN}:x64 = "\
	kernel-module-tpm-tis \
	kernel-module-atkbd \
	kernel-module-hyperv-keyboard \
	kernel-module-hv-storvsc \
	kernel-module-hv-vmbus \
	kernel-module-hv-utils \
	kernel-module-hv-balloon \
	kernel-module-i8042 \
"
