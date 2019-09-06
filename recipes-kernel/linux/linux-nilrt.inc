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

NI_RELEASE_VERSION = "master"
LINUX_VERSION = "4.14"

KBRANCH = "nilrt/${NI_RELEASE_VERSION}/${LINUX_VERSION}"

GIT_KERNEL_REPO = "linux.git"
SRC_URI = "\
	${NILRT_GIT}/${GIT_KERNEL_REPO};protocol=git;nocheckout=1;branch=${KBRANCH} \
	file://export-kernel-headers.sh \
"
SRCREV="${AUTOREV}"
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

	# on older NILRT, the kernel is not symlinked for backwards compatibility
	if [ "${DISTRO}" != nilrt-nxg ]; then
		for type in ${KERNEL_IMAGETYPES} ; do
			mv ${D}/${KERNEL_IMAGEDEST}/${type}-${KERNEL_VERSION} \
				${D}/${KERNEL_IMAGEDEST}/${type}
		done
	fi
}

do_tag_kernel_build() {
	if [ -z "${BUILDNAME}" -o "${ENABLE_BUILD_TAG_PUSH}" != "Yes" ]; then
		bbnote "Skipping git repo tagging (" $([ -z "${BUILDNAME}" ] && echo "no BUILDNAME defined" || echo "tagging not enabled by ENABLE_BUILD_TAG_PUSH") ")"
		return 0
	fi

	machine_branch="${@ get_machine_branch(d, "${KBRANCH}" )}"
	pushurl="${NILRT_RW_GIT_URI}:${GIT_KERNEL_REPO}"
	sshurl="${pushurl%:*}"

	if [ -z "${sshurl}" -o -z "${machine_branch}" ]; then
		bbfatal "Issue with push URL \"${pushurl}\" or branch \"${machine_branch}\""
	fi

	cd ${S}

	git tag -f ${BUILDNAME} ${machine_branch}

# We'll let this fail if the same tag already exists on the server (something's
# gone terribly wrong or, more likely, a minor oversight.)
	git push ${pushurl} ${BUILDNAME}
}

addtask tag_kernel_build after do_compile before do_install

pkg_postinst_ontarget_kernel-dev () {
	cd /lib/modules/${KERNEL_VERSION}/build/scripts/mod
	mv ${HOST_PREFIX}elfconfig.h elfconfig.h
	gcc -o modpost modpost.h modpost.c file2alias.c sumversion.c
}
