# This includes file contains content common to all NI implementations of the
# kernel-devsrc package.

inherit pkgconfig

DEPENDS += " elfutils"

# Export a prepared module-build tree with target-runnable Kbuild helpers so
# external modules can build without kernel preparation on the target. Use a
# private build tree to leave shared host tools untouched. Cross-build objtool
# separately because the kernel exporter copies it without rebuilding it.
do_install:append() {
	builddir=${D}${KERNEL_BUILD_ROOT}${KERNEL_VERSION}/build
	toolsdir=${WORKDIR}/target-kbuild
	hostcflags="${CPPFLAGS} ${CFLAGS} -ffile-prefix-map=${WORKDIR}=${TARGET_DBGSRC_DIR}"

	rm -rf "$toolsdir"
	install -d "$toolsdir/arch/${ARCH}"
	cp -a ${B}/.config ${B}/Module.symvers ${B}/include ${B}/scripts "$toolsdir/"
	cp -a ${B}/arch/${ARCH}/include "$toolsdir/arch/${ARCH}/"

	# Supply the exporter's input binary; replace it with a target build below.
	if grep -q '^CONFIG_OBJTOOL=y$' "$toolsdir/include/config/auto.conf"; then
		install -D -m 0755 "${B}/tools/objtool/objtool" \
			"$toolsdir/tools/objtool/objtool"
	fi

	# Regenerate the wrapper to avoid original paths embedded by alternate kernels.
	# Disable config synchronization to preserve the built kernel's configuration.
	oe_runmake --no-print-directory -C "$toolsdir" -f ${S}/Makefile O="$toolsdir" \
		may-sync-config= outputmakefile
	# Keep generators native; the exporter rebuilds installed helpers with CC.
	oe_runmake --no-print-directory -C "$toolsdir" -f ${S}/Makefile O="$toolsdir" \
		may-sync-config= \
		CROSS_COMPILE="${TARGET_PREFIX}" \
		CC="${CC}" HOSTCC="${BUILD_CC}" \
		HOSTCFLAGS="$hostcflags" \
		HOSTLDFLAGS="${LDFLAGS}" \
		run-command KBUILD_RUN_COMMAND="${S}/scripts/package/install-extmod-build $builddir"

	# ARM's generated header records an absolute input path in a comment.
	if [ "${ARCH}" = "arm" ]; then
		sed -i -e "s,${S}/arch/arm/tools/mach-types,arch/arm/tools/mach-types," \
			"$builddir/arch/${ARCH}/include/generated/asm/mach-types.h"
	fi
	if [ -x ${B}/tools/objtool/objtool ]; then
		objtool_output=${WORKDIR}/target-objtool
		rm -rf "$objtool_output"
		install -d "$objtool_output/libsubcmd" "$builddir/tools/objtool"
		# Build native fixdep for objtool and its recursive libsubcmd build.
		${BUILD_CC} ${BUILD_CFLAGS} ${BUILD_LDFLAGS} \
			-o "$objtool_output/fixdep" ${S}/tools/build/fixdep.c
		cp "$objtool_output/fixdep" "$objtool_output/libsubcmd/fixdep"
		oe_runmake -C ${S}/tools/objtool O="$objtool_output" \
			SRCARCH="${ARCH}" HOSTARCH="${ARCH}" \
			HOSTCC="${CC}" HOSTLD="${LD}" HOSTAR="${AR}" \
			HOSTPKG_CONFIG="pkg-config" \
			KBUILD_HOSTCFLAGS="$hostcflags" \
			KBUILD_HOSTLDFLAGS="${LDFLAGS}"
		install -m 0755 "$objtool_output/objtool" "$builddir/tools/objtool/objtool"
	fi

	# Avoid build-path leaks and build-user ownership in the package.
	find "$builddir/scripts" \( -name '*.o' -o -name '*.s' \) -delete
	chown -R root:root "$builddir"
}

pkg_postinst_ontarget:${PN} () {
	if [ -e /etc/kernel/postinst.d ]; then
		run-parts /etc/kernel/postinst.d -a ${KERNEL_VERSION}
	fi
}

pkg_prerm:${PN} () {
	if [ -e /etc/kernel/prerm.d ]; then
		run-parts /etc/kernel/prerm.d -a ${KERNEL_VERSION}
	fi
}

RDEPENDS:${PN}:append = " binutils gcc-symlinks"

