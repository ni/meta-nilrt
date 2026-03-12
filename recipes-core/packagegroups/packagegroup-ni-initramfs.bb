SUMMARY = "Packages common to all NILRT initramfs images."
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += "\
	base-passwd \
	bash \
	bzip2 \
	coreutils \
	dosfstools \
	e2fsprogs \
	e2fsprogs-mke2fs \
	e2fsprogs-tune2fs \
	findutils \
	fw-printenv \
	gawk \
	gptfdisk \
	grep \
	kmod \
	ni-systemreplication \
	parted \
	procps \
	sed \
	sysvinit \
	tar \
	util-linux \
	util-linux-agetty \
	vim-tiny \
"

# TPM Interaction
RDEPENDS:${PN}:x64 += "\
	cryptsetup \
	libtss2-tcti-device \
	ni-device-encryption \
	tpm2-tools \
"

RDEPENDS:${PN}:append:x64 = "\
	dmidecode           \
	efibootmgr          \
	efivar              \
	eudev               \
	grub                \
	grub-editenv        \
	grub-efi            \
	ni-smbios-helper    \
"

RDEPENDS:${PN}:append:xilinx-zynq = "\
	mtd-utils           \
	mtd-utils-ubifs     \
"
