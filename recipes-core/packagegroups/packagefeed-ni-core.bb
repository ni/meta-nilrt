SUMMARY = "Package feed which contains all packages which are supported by NI."
LICENSE = "MIT"

inherit packagegroup

RDEPENDS:${PN} = "\
	packagegroup-base \
	packagegroup-core-boot \
	packagegroup-core-standalone-sdk-target \
	packagegroup-kernel-module-build \
	packagegroup-ni-base \
	packagegroup-ni-contributors \
	packagegroup-ni-crio \
	packagegroup-ni-initramfs \
	packagegroup-ni-internal-deps \
	packagegroup-ni-ptest-smoke \
	packagegroup-ni-restoremode \
	packagegroup-ni-runmode \
	packagegroup-ni-safemode \
	packagegroup-ni-skyline \
	packagegroup-ni-tzdata \
	packagegroup-ni-wifi \
	dkms \
"

RDEPENDS:${PN}:append = "\
	docker \
"

RDEPENDS:${PN}:append:x64 = "\
	nilrt-grub-runmode \
	nilrt-grub-safemode \
	packagegroup-core-x11 \
	packagegroup-ni-graphical \
	packagegroup-ni-nohz-kernel \
	packagegroup-ni-snac \
	packagegroup-ni-tpm \
	bolt \
	onboard \
	env-config-container \	
"

# Image-specific packages kept separately so they are not accidentally
# installed via a packagegroup dependency into the wrong image.
RDEPENDS:${PN}:append:x64 = "\
	init-nilrt-runmode-initramfs \
"

# Image-specific packages kept separately so they are not accidentally
# installed via a packagegroup dependency into the wrong image.
RDEPENDS:${PN}:append:x64 = "\
	init-nilrt-runmode-initramfs \
"
