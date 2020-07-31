# (C) Copyright 2016,
# National Instruments Corporation.
# All rights reserved.

SUMMARY = "Ptest packages for the NI Linux Realtime distribution"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} = "ptest-runner"

RDEPENDS_${PN} += "packagegroup-ni-ptest-smoke"

# other supported ptest packages
RDEPENDS_${PN}_append = "\
"

RDEPENDS_${PN}_append_nilrt-nxg = "\
"
