# (C) Copyright 2016,
# National Instruments Corporation.
# All rights reserved.

SUMMARY = "Ptest packages for the NI Linux Realtime distribution"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} = "ptest-runner"

# ptest packages
RDEPENDS_${PN}_append = "\
    distro-feed-configs-ptest \
    glibc-tests-ptest \
    hwclock-init-ptest \
    kernel-tests-ptest \
    opkg-ptest \
    rt-tests-ptest \
    run-postinsts-ptest \
    salt-ptest \
"
