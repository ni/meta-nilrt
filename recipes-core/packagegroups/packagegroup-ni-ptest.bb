# (C) Copyright 2016,
# National Instruments Corporation.
# All rights reserved.

SUMMARY = "Ptest packages for the NI Linux Realtime distribution"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} = "ptest-runner"

RDEPENDS:${PN} += "packagegroup-ni-ptest-smoke"

# other supported ptest packages
RDEPENDS:${PN}:append = "\
"

RDEPENDS:${PN}:append:nilrt-nxg = "\
"
