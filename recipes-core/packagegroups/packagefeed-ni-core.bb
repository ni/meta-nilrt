SUMMARY = "Package feed which contains all packages which are supported by NI."
LICENSE = "MIT"

inherit packagegroup

RDEPENDS:${PN} = "\
	packagegroup-base \
	packagegroup-core-boot \
	packagegroup-ni-base \
	packagegroup-ni-contributors \
	packagegroup-ni-crio \
	packagegroup-ni-graphical \
	packagegroup-ni-internal-deps \
	packagegroup-ni-nohz-kernel \
	packagegroup-ni-restoremode \
	packagegroup-ni-runmode \
	packagegroup-ni-safemode \
	packagegroup-ni-transconf \
	packagegroup-ni-tzdata \
	packagegroup-ni-wifi \
	packagegroup-core-x11 \
	packagegroup-core-standalone-sdk-target \
	packagegroup-kernel-module-build \
	dkms \
"

RDEPENDS:${PN}:append:x64 = "\
	init-nilrt-ramfs \
	rauc \
	rauc-mark-good \
	nilrt-grub-runmode \
	nilrt-grub-safemode \
"
