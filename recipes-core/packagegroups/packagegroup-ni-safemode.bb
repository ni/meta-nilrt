# (C) Copyright 2019,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "Safemode specific packages for NI Linux Realtime distribution"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} = " \
	${@bb.utils.contains('INIT_MANAGER', 'systemd', 'services-nilrt-safemode', 'initscripts-nilrt-safemode', d)} \
	e2fsprogs \
	e2fsprogs-e2fsck \
	e2fsprogs-mke2fs \
	e2fsprogs-tune2fs \
	ni-netcfgutil \
	ni-shutdown-guard-safemode \
	ni-systemimage \
	sysconfig-settings-ssh \
"

# GPU firmware, included as split packages to conserve space
#
# Intel Valleyview family, no i915 firmware in use
#   cRIO-903x, CVS-1458RT, CVS-1459RT, IC-312x
#   sbRIO-960x, sbRIO-962x, sbRIO-9638, cDAQ-913x
#
# Intel Broadwell family, no i915 firmware in use
#   IC-317x
#
# Intel Haswell family, no i915 firmware in use
#   PXIe-8821, PXIe-8840
#
# Intel Broxton family, uses 'bxt' dmc firmware, GuC and HuC disabled
#   cRIO-904x
#
# Intel Tiger Lake family, uses 'tgl' dmc firmware, GuC and HuC disabled
#   PXIe-8822, PXIe-8842, PXIe-8862
#
# N/A, no video output
#   cRIO-905x
#
# N/A, radeon GPU
#   PXIe-8861, PXIe-8880, PXIe-8881
#
RDEPENDS:${PN}:append:x64 = "\
	linux-firmware-i915-bxt-dmc \
	linux-firmware-i915-tgl-dmc \
"
