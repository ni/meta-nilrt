SUMMARY = "Dynamic Kernel Module System (DKMS)"
DESCRIPTION = "DKMS is a framework designed to allow individual kernel modules to be upgraded without changing the whole kernel. It is also very easy to rebuild modules as you upgrade kernels."
HOMEPAGE = "https://github.com/dell/dkms/"
BUGTRACKER = "https://github.com/dell/dkms/issues"
SECTION = "kernel"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"


PV = "3.0.5"


SRC_URI = "git://github.com/dell/dkms.git;protocol=https;branch=master \
           file://0001-Makefile-set-release-information-for-v3.0.5.patch \
           file://0002-dkms.in-skip-sign_file-call-when-unset.patch \
           "

SRCREV = "1e24a54acaa2e3a8aa6e8e24b21f47553370c4fe"

S = "${WORKDIR}/git"


inherit autotools-brokensep

# We don't need the dist/ tarball.
EXTRA_OEMAKE += " -o tarball"

INSANE_SKIP:${PN} += "dev-deps"


FILES:${PN} += " ${datadir}/bash-completion/*"

RDEPENDS:${PN} += " \
	bash \
	gcc \
	kernel-dev \
	kernel-devsrc \
	kmod \
	make \
	patch \
"

