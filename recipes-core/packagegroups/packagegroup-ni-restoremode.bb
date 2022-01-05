SUMMARY = "initramfs specific packages for NI Linux Realtime distribution"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} += "\
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
	eudev \
	findutils \
	fw-printenv \
	gawk \
	gptfdisk-sgdisk \
	grep \
	grub \
	grub-editenv \
	grub-efi \
	init-restore-mode \
	kmod \
	ni-smbios-helper \
	nilrtdiskcrypt \
	parted \
	procps \
	sed \
	sysvinit \
	tar \
	util-linux \
	util-linux-agetty \
	vim-tiny \
"

RRECOMMENDS_${PN}_x64 = "\
	kernel-module-tpm-tis \
	kernel-module-atkbd \
	kernel-module-hyperv-keyboard \
	kernel-module-hv-storvsc \
	kernel-module-hv-vmbus \
	kernel-module-hv-utils \
	kernel-module-hv-balloon \
	kernel-module-i8042 \
"
