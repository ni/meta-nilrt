# This includes file contains content common to all NI implementations of the
# kernel-devsrc package.

pkg_postinst_ontarget_${PN} () {
	cd /lib/modules/${KERNEL_VERSION}/build
	find ./ -exec touch -m {} \;
	make olddefconfig
	make prepare
	make modules_prepare

	if [ -e /etc/kernel/postinst.d ]; then
		run-parts /etc/kernel/postinst.d -a ${KERNEL_VERSION}
	fi
}

pkg_prerm_${PN} () {
	if [ -e /etc/kernel/prerm.d ]; then
		run-parts /etc/kernel/prerm.d -a ${KERNEL_VERSION}
	fi
}

RDEPENDS_${PN}_append += "binutils gcc-symlinks"

# "make prepare" skips building objtool if elfutils-dev is not installed at
# postinst time; however if you install elfutils-dev later, then when dkms
# tries to build modules it expects objtool to be available, but it's not.
# Depending on elfutils-dev forces it to be there during postinst.
RDEPENDS_${PN}_append += "${@bb.utils.contains('ARCH', 'x86', 'elfutils-dev', '', d)}"
