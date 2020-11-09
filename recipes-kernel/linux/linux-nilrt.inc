inherit kernel
require recipes-kernel/linux/linux-yocto.inc

machine_srcrev="${SRCREV}"

# Set the version for traceability purposes
LINUX_VERSION_EXTENSION = "$([ -z "${BUILDNAME}" ] || echo '-')${BUILDNAME}"

# Setting EXTRA_OEMAKE to include CFLAGS settings is required as
# the Kbuild system will clobber CC (which is used by OE for setting
# the compiler and flags to the compiler/linker/etc.)
EXTRA_OEMAKE += 'CFLAGS="${TOOLCHAIN_OPTIONS}"'

# Kbuild intenionally clobbers CFLAGS/LDFLAGS for some binaries we ship in nilrt,
# particularly in the tools/ dir (fixdep, genksyms etc), and OE has this insanity check
# to see if binaries were built using the OE-supplied *FLAGS, so skip this sanity check to
# avoid this OE<>Kernel clash (linux-yocto recipe doesn't have this problem because it does
# not ship tools like we do for our 'on target versioning')
INSANE_SKIP += "ldflags"

# The default -hf arm tools don't default to -mfloat-abi=hard, and this
# causes build failures when building the scripts utilities
TOOLCHAIN_OPTIONS_append = " ${@bb.utils.contains('TUNE_FEATURES','callconvention-hard',' -mfloat-abi=hard ','',d)} ${CFLAGS} "

# No need to do debug symbol handling for kernel builds; also the default behavior
# incorrectly splits out some 'kernel-dev' files under /lib/modules/<ver>/build
INHIBIT_PACKAGE_DEBUG_SPLIT="1"

# The NI kernel build includes on-target versioning tools that
# link against the gcc provided runtime
do_kernel_configme[depends] += "libgcc:do_populate_sysroot"

SRC_URI = "\
	${NILRT_GIT}/${GIT_KERNEL_REPO};protocol=git;nocheckout=1;branch=${KBRANCH} \
	file://export-kernel-headers.sh \
"
SRCREV="997fc3df5a3e740f585e6298cae9bd9d63832f7a"
PV = "${LINUX_VERSION}+git${SRCPV}"

# This checks ${PV} == version from kernel sources which our PV/AUTOREV breaks, so skip it.
KERNEL_VERSION_SANITY_SKIP="1"

FILESEXTRAPATHS_prepend := "${THISDIR}/${KBUILD_FRAGMENTS_LOCATION}:"
KBUILD_DEFCONFIG_x64 = "nati_x86_64_defconfig"
KBUILD_DEFCONFIG_armv7a = "nati_zynq_defconfig"
KCONFIG_MODE="--alldefconfig"

# i915 video driver module options to disable render and display power saving
# disabling them improves real-time determinism
KERNEL_MODULE_PROBECONF += "i915"
module_conf_i915 = "options i915 enable_rc6=0 enable_dc=0"

# radeon video driver module options to disable (broken) dynamic power management
KERNEL_MODULE_PROBECONF += "radeon"
module_conf_radeon = "options radeon dpm=0"

#create dtb-s fromt dts files for armv7a targets
DEPENDS_append += "rsync-native"

DEPENDS_append_x64 += "elfutils-native"

DEPENDS_append_armv7a += "dtc-native"

RDEPENDS_kernel-dev += "bash binutils gcc gcc-symlinks"

PACKAGES += "${PN}-module-versioning-headers"

FILES_${PN}-module-versioning-headers = "/kernel"

# Install headers and build objects for kernel versioning into
do_install_append() {
	export CROSS_COMPILE=${CROSS_COMPILE} ARCH=${ARCH} KBUILD_OUTPUT=${KBUILD_OUTPUT}

	# for NXG NILRT install under /lib/modules/ to be included in kernel-dev pkg
	${WORKDIR}/export-kernel-headers.sh ${S} ${D}${base_libdir}/modules/${KERNEL_VERSION}/build

	# for older NILRT install under /kernel for inclusion in squashfs for backwards
	# compatibility with non-standard Linux installs (non-dkms). This should go away
	# and older NILRT should be migrated to kernel-dev + dkms in the future.
	${WORKDIR}/export-kernel-headers.sh ${S} ${D}/kernel
}

pkg_postinst_ontarget_${KERNEL_PACKAGE_NAME}-base_append_x64 () {
	cd "$D/${KERNEL_IMAGEDEST}"

	if [ -f "${KERNEL_IMAGETYPE}" ]; then
		if [ -L "${KERNEL_IMAGETYPE}" ]; then
			# If the current kernel image file (e.g. bzImage) is a symlink check
			# if the kernel version is stored in the 'kernel-versions' file (for uninstall)
			kpath=`readlink -f ${KERNEL_IMAGETYPE} 2>/dev/null`
			if [ -f "$kpath" ]; then
				kimage=`basename $kpath 2>/dev/null`
				if ! grep -xqs "$kimage" kernel-versions; then
					echo "$kimage" >> kernel-versions
				fi
			fi
		else
			# If we have an actual kernel image file (e.g. bzImage) and it's not
			# a symlink then it must be the kernel currently booted; rename it
			# and store its version for uninstall.
			if mv "${KERNEL_IMAGETYPE}" "${KERNEL_IMAGETYPE}-`uname -r`" 2>/dev/null; then
				echo "${KERNEL_IMAGETYPE}-`uname -r`" >> kernel-versions
			else
				echo "WARNING: could not move existing $D/${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE} file"
			fi
		fi
	fi

	echo "${KERNEL_IMAGETYPE}-${KERNEL_VERSION}" >> kernel-versions

	if [ -L "${KERNEL_IMAGETYPE}" -o ! -f "${KERNEL_IMAGETYPE}" ]; then
		ln -f -s "${KERNEL_IMAGETYPE}-${KERNEL_VERSION}" "${KERNEL_IMAGETYPE}"
	else
		echo "ERROR: $D/${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE} file is not a symbolic link"
		echo "ERROR: cannot switch to the new kernel image $D/${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE}-${KERNEL_VERSION}"
		exit 1
	fi
}

pkg_prerm_${KERNEL_PACKAGE_NAME}-base_append_x64 () {
	cd "$D/${KERNEL_IMAGEDEST}"

	# Check that the current kernel image file (e.g. bzImage) is a
	# symlink and make sure is listed in the 'kernel-versions'
	# file. This will also create the 'kernel-versions' file if it
	# doesn't exist.
	if [ -L "${KERNEL_IMAGETYPE}" ]; then
		kpath=`readlink -f ${KERNEL_IMAGETYPE} 2>/dev/null`
		if [ -f "$kpath" ]; then
			kimage=`basename $kpath 2>/dev/null`
			if ! grep -xqs "$kimage" kernel-versions; then
				echo "$kimage" >> kernel-versions
			fi
		fi
	else
		echo "Error: $D/${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE} is not a symlink."
		echo "Refusing to uninstall default kernel"
		exit 1;
	fi

	# Also make sure the kernel being uninstalled is listed in
	# 'kernel-versions'. This simplifies error checking later.
	if ! grep -xqs "${KERNEL_IMAGETYPE}-${KERNEL_VERSION}" kernel-versions; then
		echo "${KERNEL_IMAGETYPE}-${KERNEL_VERSION}" >> kernel-versions
	fi

	# Sanity check the kernels listed in 'kernel-versions' are
	# present on disk.
	while read line; do
		if [ -f "$line" ]; then
			echo "$line"
		else
			echo "WARNING: kernel '$line' listed in '$D/${KERNEL_IMAGEDEST}/kernel-versions' not found on disk" 1>&2
		fi
	done <kernel-versions >kernel-versions.tmp
	mv -f kernel-versions.tmp kernel-versions

	# Make sure this is not the last kernel available before proceeding.
	if [ $(wc -l <kernel-versions) -le 1 ]; then
		echo "Error: Refusing to uninstall last available kernel."
		exit 1
	fi
}

pkg_postrm_${KERNEL_PACKAGE_NAME}-base_append_x64 () {
	cd "$D/${KERNEL_IMAGEDEST}"

	# Remove kernel name from 'kernel-versions' file. We're
	# relying on the prerm script for sanity checks (e.g. bzImage
	# is a symlink we have another kernel to fall back to etc.)
	while read line; do
		if [ ! "$line" = "${KERNEL_IMAGETYPE}-${KERNEL_VERSION}" ]; then
			echo "$line"
		fi
	done <kernel-versions >kernel-versions.tmp
	mv -f kernel-versions.tmp kernel-versions

	# If the symlink points to this kernel; update it.
	kpath=`readlink -f ${KERNEL_IMAGETYPE} 2>/dev/null`
	if [ -f "$kpath" ]; then
		kimage=`basename $kpath 2>/dev/null`
		if [ "$kimage" = "${KERNEL_IMAGETYPE}-${KERNEL_VERSION}" ]; then
			ln -f -s `tail -1 kernel-versions` "${KERNEL_IMAGETYPE}"
		fi
	fi
}


pkg_postinst_ontarget_kernel-dev () {
	cd /lib/modules/${KERNEL_VERSION}/build/scripts/mod
	mv ${HOST_PREFIX}elfconfig.h elfconfig.h
	gcc -o modpost modpost.h modpost.c file2alias.c sumversion.c
}
