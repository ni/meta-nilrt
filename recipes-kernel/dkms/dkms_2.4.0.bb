SUMMARY = "DKMS Recipe - Adds DKMS tool for target"
HOMEPAGE = "https://github.com/dell/dkms/"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=0636e73ff0215e8d672dc4c32c317bb3"

SRC_URI = "https://github.com/dell/${PN}/archive/v${PV}.tar.gz"

SRC_URI[md5sum] = "d2e74dd79086c564a924b5763794091b"

INSANE_SKIP_${PN} += "dev-deps"

RDEPENDS_${PN} += "bash kmod gcc make patch kernel-dev"

inherit autotools-brokensep
