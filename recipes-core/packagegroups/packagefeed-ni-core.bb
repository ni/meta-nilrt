SUMMARY = "Package feed which contains all packages which are supported by NI."
LICENSE = "MIT"

inherit packagegroup

RDEPENDS:${PN} = "\
	packagegroup-base \
	packagegroup-core-boot \
	packagegroup-core-standalone-sdk-target \
	packagegroup-core-x11 \
	packagegroup-kernel-module-build \
	packagegroup-ni-base \
	packagegroup-ni-contributors \
	packagegroup-ni-crio \
	packagegroup-ni-graphical \
	packagegroup-ni-internal-deps \
	packagegroup-ni-nohz-kernel \
	packagegroup-ni-ptest-smoke \
	packagegroup-ni-restoremode \
	packagegroup-ni-runmode \
	packagegroup-ni-safemode \
	packagegroup-ni-skyline \
	packagegroup-ni-snac \
	packagegroup-ni-tzdata \
	packagegroup-ni-wifi \
	dkms \
	bolt \
	onboard \
"

RDEPENDS:${PN}:append = "\
	docker \
"

RDEPENDS:${PN}:append:x64 = "\
	init-nilrt-ramfs \
	nilrt-grub-runmode \
	nilrt-grub-safemode \
"
