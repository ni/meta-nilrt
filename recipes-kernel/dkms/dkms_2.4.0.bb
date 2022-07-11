SUMMARY = "Dynamic Kernel Module System (DKMS)"
DESCRIPTION = "DKMS is a framework designed to allow individual kernel modules to be upgraded without changing the whole kernel. It is also very easy to rebuild modules as you upgrade kernels."
HOMEPAGE = "https://github.com/dell/dkms/"
BUGTRACKER = "https://github.com/dell/dkms/issues"
SECTION = "kernel"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=0636e73ff0215e8d672dc4c32c317bb3"

SRC_URI = "git://github.com/dell/dkms.git;protocol=https;branch=master"

SRCREV = "8c3065c6b26d573d55abfcb17b422204ba63e590"

S = "${WORKDIR}/git"

INSANE_SKIP_${PN} += "dev-deps"

RDEPENDS_${PN} += " \
	bash \
	gcc \
	kernel-dev \
	kernel-devsrc \
	kmod \
	make \
	patch \
"

inherit autotools-brokensep
