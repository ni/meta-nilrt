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
	dmidecode \
	dosfstools \
	e2fsprogs \
	e2fsprogs-mke2fs \
	e2fsprogs-tune2fs \
	efibootmgr \
	efivar \
	findutils \
	fw-printenv \
	gawk \
	gptfdisk \
	grep \
	grub \
	grub-editenv \
	grub-efi \
	init-restore-mode \
	kmod \
	ni-smbios-helper \
	ni-systemreplication \
	parted \
	procps \
	sed \
	tar \
	util-linux \
	util-linux-agetty \
	vim-tiny \
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
