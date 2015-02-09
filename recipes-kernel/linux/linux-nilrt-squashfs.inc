# Installs module-versioning-image.squashfs to /usr/local/natinst/tools
# and runs the NI-KAL versioning against existing installed KAL-based drivers

DEPENDS =+ "squashfs-tools-native"

KERNEL_SQUASHFS = "module-versioning-image.squashfs"
KERNEL_SQUASHFS_DESTDIR = "/usr/local/natinst/tools"
KERNEL_SQUASHFS_DEST = "${KERNEL_SQUASHFS_DESTDIR}/${KERNEL_SQUASHFS}"
KERNEL_SQUASHFS_BUILDDIR = "${WORKDIR}/squashfs-build"

FILESEXTRAPATHS_prepend := "${THISDIR}/squashfs:"
SRC_URI += "file://export-kernel-headers.sh \
            file://export-module-versioning-img.sh \
           "

PACKAGES_append = " kernel-module-versioning "
RDEPENDS_kernel_append = " kernel-module-versioning "
FILES_kernel-module-versioning = "${KERNEL_SQUASHFS_DEST}"
PKG_kernel-module-versioning = "kernel-module-versioning-${@legitimize_package_name('${KERNEL_VERSION}')}"
RDEPENDS_kernel-module-versioning = " ${KERNEL_MODULE_PACKAGE_NAME_PREPEND}-squashfs "

addtask kernel_make_squashfs after do_compile_kernelmodules before do_strip

do_kernel_make_squashfs() {
    mkdir -p ${KERNEL_SQUASHFS_BUILDDIR}/kernel
    ${WORKDIR}/export-kernel-headers.sh ${S} ${KBUILD_OUTPUT} ${KERNEL_SQUASHFS_BUILDDIR}/kernel ${ARCH}
	cp ${KBUILD_OUTPUT}/Module.symvers ${KERNEL_SQUASHFS_BUILDDIR}/kernel/
    ${WORKDIR}/export-module-versioning-img.sh mksquashfs ${KERNEL_SQUASHFS_BUILDDIR}/kernel ${KERNEL_SQUASHFS_BUILDDIR}/squashfs ${KERNEL_SQUASHFS_BUILDDIR}/${KERNEL_SQUASHFS} ${HOST_PREFIX}
}

do_install_append() {
	install -d ${D}${KERNEL_SQUASHFS_DESTDIR}
	install -m 0644 ${KERNEL_SQUASHFS_BUILDDIR}/${KERNEL_SQUASHFS} ${D}${KERNEL_SQUASHFS_DEST}
}

# Original squashfs image was not installed via opkg.
# Rename and preserve the existing file.
pkg_preinst_kernel-module-versioning() {
	mv ${KERNEL_SQUASHFS_DEST} ${KERNEL_SQUASHFS_DEST}.orig || true
}

pkg_postinst_kernel-module-versioning() {
	[ -e /usr/local/natinst/tools/versioning_utils.sh ] && source /usr/local/natinst/tools/versioning_utils.sh
	rm -f /usr/local/natinst/tools/kernel_version
	setup_versioning_env
	versioning_call /usr/local/natinst/nikal/bin/updateNIDrivers --no-prompt --linuxrt `kernel_version`
	cleanup_versioning_env
}

pkg_postrm_kernel-module-versioning() {
	mv ${KERNEL_SQUASHFS_DEST}.orig ${KERNEL_SQUASHFS_DEST} || true
	[ -e /usr/local/natinst/tools/versioning_utils.sh ] && source /usr/local/natinst/tools/versioning_utils.sh
	rm -f /usr/local/natinst/tools/kernel_version
	setup_versioning_env
	versioning_call /usr/local/natinst/nikal/bin/updateNIDrivers --no-prompt --linuxrt `kernel_version`
	cleanup_versioning_env
}
