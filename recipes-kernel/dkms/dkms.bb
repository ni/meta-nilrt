SUMMARY = "Dynamic Kernel Module System (DKMS)"
DESCRIPTION = "DKMS is a framework designed to allow individual kernel modules to be upgraded without changing the whole kernel. It is also very easy to rebuild modules as you upgrade kernels."
HOMEPAGE = "https://github.com/dell/dkms/"
BUGTRACKER = "https://github.com/dell/dkms/issues"
SECTION = "kernel"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"


PV = "3.0.6"


SRC_URI = " \
	git://github.com/dell/dkms.git;protocol=https;branch=master \
"

SRCREV = "8ff0bc21594fba4edac7173d11f02710044a5113"

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
