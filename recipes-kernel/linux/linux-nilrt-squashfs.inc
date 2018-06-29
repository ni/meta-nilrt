# Installs module-versioning-image.squashfs to /usr/local/natinst/tools
# and runs the NI-KAL versioning against existing installed KAL-based drivers
FILESEXTRAPATHS_prepend := "${THISDIR}/squashfs:"

SRC_URI += "\
	file://export-kernel-headers.sh \
	file://export-module-versioning-img.sh \
"

DEPENDS =+ "squashfs-tools-native"

NATINST_PATH = "/usr/local/natinst"
KERNEL_SQUASHFS = "module-versioning-image.squashfs"
KERNEL_SQUASHFS_DESTDIR = "${NATINST_PATH}/tools"
KERNEL_SQUASHFS_DEST = "${KERNEL_SQUASHFS_DESTDIR}/${KERNEL_SQUASHFS}"
KERNEL_SQUASHFS_BUILDDIR = "${WORKDIR}/squashfs-build"
NIKAL_BIN_PATH = "${NATINST_PATH}/nikal/bin"

PACKAGES += "kernel-module-versioning"

RDEPENDS_kernel-module-versioning = "${KERNEL_PACKAGE_NAME}-module-squashfs"
RDEPENDS_${KERNEL_PACKAGE_NAME} += "kernel-module-versioning"

FILES_kernel-module-versioning = "${KERNEL_SQUASHFS_DEST}"

PKG_kernel-module-versioning = "kernel-module-versioning-${@legitimize_package_name('${KERNEL_VERSION}')}"

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
	[ -e ${KERNEL_SQUASHFS_DESTDIR}/versioning_utils.sh ] && source ${KERNEL_SQUASHFS_DESTDIR}/versioning_utils.sh
	rm -f ${KERNEL_SQUASHFS_DESTDIR}/kernel_version
	setup_versioning_env
	versioning_call ${NIKAL_BIN_PATH}/updateNIDrivers --no-prompt --linuxrt `kernel_version`
	cleanup_versioning_env
}

pkg_postrm_kernel-module-versioning() {
	mv ${KERNEL_SQUASHFS_DEST}.orig ${KERNEL_SQUASHFS_DEST} || true
	[ -e ${KERNEL_SQUASHFS_DESTDIR}/versioning_utils.sh ] && source ${KERNEL_SQUASHFS_DESTDIR}/versioning_utils.sh
	rm -f ${KERNEL_SQUASHFS_DESTDIR}/kernel_version
	setup_versioning_env
	versioning_call ${NIKAL_BIN_PATH}/updateNIDrivers --no-prompt --linuxrt `kernel_version`
	cleanup_versioning_env
}
