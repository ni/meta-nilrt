SUMMARY = "Dynamic Kernel Module System (DKMS)"
DESCRIPTION = "DKMS is a framework designed to allow individual kernel modules to be upgraded without changing the whole kernel. It is also very easy to rebuild modules as you upgrade kernels."
HOMEPAGE = "https://github.com/dell/dkms/"
BUGTRACKER = "https://github.com/dell/dkms/issues"
SECTION = "kernel"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=570a9b3749dd0463a1778803b12a6dce"


PV = "3.0.13"


SRC_URI = "\
	git://github.com/dell/dkms.git;protocol=https;branch=main \
	file://0001-autoinstall-all-kernels.patch \
"

SRCREV = "6e32f352f3d8c7ccbc6fecb05b1517248a2f3934"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
	oe_runmake install DESTDIR=${D} LIBDIR=${libdir} 
}

# We don't need the dist/ tarball.
EXTRA_OEMAKE += " -o tarball"

INSANE_SKIP:${PN} += "dev-deps"

FILES:${PN} += "${libdir}/dkms_autoinstaller \
				${libdir}/common.postinst \ 
				${datadir}/bash-completion/* \
				${datadir}/zsh/* \
"

RDEPENDS:${PN} += " \
	bash \
	gcc \
	kernel-dev \
	kernel-devsrc \
	kmod \
	make \
	patch \
"

