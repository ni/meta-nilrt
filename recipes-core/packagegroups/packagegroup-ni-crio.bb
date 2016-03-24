# (C) Copyright 2015,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "CompactRIO support packages for NI Linux Real-Time distribution"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} = "\
    crio-support-scripts \
    niwatchdogpet \
"
