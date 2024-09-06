SUMMARY = "Dynamic Kernel Module System (DKMS)"
DESCRIPTION = "DKMS is a framework designed to allow individual kernel modules to be upgraded without changing the whole kernel. It is also very easy to rebuild modules as you upgrade kernels."
HOMEPAGE = "https://github.com/dell/dkms/"
BUGTRACKER = "https://github.com/dell/dkms/issues"
SECTION = "kernel"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"


PV = "3.0.13"


SRC_URI = "\
	git://github.com/dell/dkms.git;protocol=https;branch=master \
	file://0001-autoinstall-all-kernels.patch \
"

SRCREV = "4d466bf727347408307aa28ab4f090488360b592"

S = "${WORKDIR}/git"


inherit autotools-brokensep

# We don't need the dist/ tarball.
EXTRA_OEMAKE += " -o tarball"

INSANE_SKIP:${PN} += "dev-deps"


FILES:${PN} += " ${datadir}/bash-completion/* ${datadir}/zsh/*"

RDEPENDS:${PN} += " \
	bash \
	gcc \
	kernel-dev \
	kernel-devsrc \
	kmod \
	make \
	patch \
"

