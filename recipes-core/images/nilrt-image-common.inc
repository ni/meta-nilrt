DESCRIPTION ?= "Base image setup used in all NILRT images"
LICENSE = "MIT"

IMAGE_FSTYPES = "${INITRAMFS_FSTYPES} tar.bz2 ext2 wic"

IMAGE_FEATURES ??= ""
IMAGE_LINGUAS ??= ""

IMAGE_PREPROCESS_COMMAND += "rootfs_update_timestamp;"

ROOTFS_POSTPROCESS_COMMAND += "\
	${@oe.utils.conditional('DISTRO', 'nilrt-nxg', '', 'prune_unused_packages; remove_kernel;', d)} \
	remove_unused_feeds; \
	symlink_python; \
"

PACKAGE_REMOVE ??= "kernel-*"

prune_unused_packages() {
	opkg -o ${IMAGE_ROOTFS} -f ${IPKGCONF_TARGET} --force-depends remove ${PACKAGE_REMOVE};
}

remove_unused_feeds() {
	rm -f ${IMAGE_ROOTFS}/var/lib/opkg/lists/*
}

remove_kernel() {
	rm -rf ${IMAGE_ROOTFS}/boot/*
	rm -f ${IMAGE_ROOTFS}/usr/lib/opkg/alternatives/bzImage
	rm -f ${IMAGE_ROOTFS}/usr/lib/opkg/alternatives/zImage
}

# OE-core does not yet support shipping python 3 as default python (symlinked) and the
# python recipes are written in such a way that all packages are generated from a hardcoded
# file manifest (in which python2 is symlinked to /usr/bin/python) and can't be modified in
# pkg_postinst's. Since the python 3 default is a NILRT config, we do it here instead of
# hardcoding it in the manifest and convincing everyone using OE to default to python 3 or
# keeping that change downstream in OE-core.

# TODO: remove once OE-core decides to symlink python3 as /usr/bin/python instead of 2.
symlink_python() {
	if [ -f ${IMAGE_ROOTFS}/${bindir}/python3 ]; then
		ln -sf ${bindir}/python3 ${IMAGE_ROOTFS}/${bindir}/python
	fi
}

inherit core-image
