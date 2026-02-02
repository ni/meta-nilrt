# (C) Copyright 2026,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "TPM packages for NI Linux Realtime distribution"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} = "\
	packagegroup-security-tpm2 \
	clevis \
	cryptsetup \
	libtss2-tcti-device \
"
