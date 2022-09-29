# (C) Copyright 2016,
# National Instruments Corporation.
# All rights reserved.

SUMMARY = "Ptest packages necessary for passing the NILRT development pipeline"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} = "ptest-runner"

# ptest packages
RDEPENDS:${PN}:append = "\
    glibc-tests-ptest \
    kernel-tests-ptest \
    opkg-ptest \
    pstore-save-ptest \
    rt-tests-ptest \
    run-postinsts-ptest \
"
