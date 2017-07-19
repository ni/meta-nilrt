inherit kernel
require recipes-kernel/linux/linux-yocto.inc

machine_srcrev="${SRCREV}"

# Set the version for traceability purposes
LINUX_VERSION_EXTENSION = "$([ -z "${BUILD_IDENTIFIER}" ] || echo '-')${BUILD_IDENTIFIER}"

# Setting EXTRA_OEMAKE to include CFLAGS settings is required as
# the Kbuild system will clobber CC (which is used by OE for setting
# the compiler and flags to the compiler/linker/etc.)
EXTRA_OEMAKE_append += 'CFLAGS="${TOOLCHAIN_OPTIONS}"'

# The default -hf arm tools don't default to -mfloat-abi=hard, and this
# causes build failures when building the scripts utilities
TOOLCHAIN_OPTIONS_append = " ${@bb.utils.contains('TUNE_FEATURES','callconvention-hard',' -mfloat-abi=hard ','',d)} ${CFLAGS} "

# No need to do debug symbol handling for kernel builds; also the default behavior
# incorrectly splits out some 'kernel-dev' files under /lib/modules/<ver>/build
INHIBIT_PACKAGE_DEBUG_SPLIT="1"

# The NI kernel build includes on-target versioning tools that
# link against the gcc provided runtime
do_kernel_configme[depends] += "libgcc:do_populate_sysroot"

GIT_KERNEL_REPO = "linux.git"
SRC_URI = "${NILRT_GIT}/${GIT_KERNEL_REPO};protocol=git;nocheckout=1;branch=${KBRANCH}"
SRCREV="${AUTOREV}"
PV = "${LINUX_VERSION}+git${SRCPV}"

# This checks ${PV} == version from kernel sources which our PV/AUTOREV breaks, so skip it.
KERNEL_VERSION_SANITY_SKIP="1"

FILESEXTRAPATHS_prepend := "${THISDIR}/${KBUILD_FRAGMENTS_LOCATION}:"

KBUILD_DEFCONFIG_x64 = "nati_x86_64_defconfig"
KBUILD_DEFCONFIG_armv7a = "nati_zynq_defconfig"
KCONFIG_MODE="--alldefconfig"

#create dtb-s fromt dts files for armv7a targets
DEPENDS_armv7a += "dtc-native"

KERNEL_DEVICETREE_xilinx-zynqhf = "\
        ni-762F.dtb \
        ni-76D3.dtb \
        ni-76D6.dtb \
        ni-76F2.dtb \
        ni-7740.dtb \
        ni-7741.dtb \
        ni-7742.dtb \
        ni-7743.dtb \
        ni-7744.dtb \
        ni-774C.dtb \
        ni-774E.dtb \
        ni-775E.dtb \
        ni-77AC.dtb \
        ni-77B1.dtb \
        ni-77B2.dtb \
        ni-77D4.dtb \
        ni-77D5.dtb \
        ni-77D6.dtb \
        ni-7885.dtb \
"

KERNEL_DEVICETREE_n310 = " \
        zynq-ni-sulfur-rev3.dtb \
"

#create uImage for armv7a targets
KERNEL_IMAGETYPE_armv7a = "uImage"
UBOOT_LOADADDRESS ?= "0x8000"
UBOOT_ENTRYPOINT ?= "${UBOOT_LOADADDRESS}"
KERNEL_EXTRA_ARGS_armv7a += "LOADADDR=0x8000"

# Install headers and build objects for kernel versioning into
# /lib/modules/${KERNEL_VERSION}/build for inclusion in kernel-dev.
do_install_append() {
	kerneldevdir=${D}/lib/modules/${KERNEL_VERSION}/build
	install -d $kerneldevdir

	cd ${S}

	# Copy Makefile, Kconfig files
	rsync -avm \
	--chown=0:0 \
	--include="/arch/${ARCH}/" \
	--exclude="/arch/*/" \
	--exclude="/Documentation/" \
	--exclude="/scripts/" \
	--include="*/" \
	--include="Makefile*" \
	--include="Kconfig*" \
	--exclude="*" \
	. $kerneldevdir

	# Copy headers
	rsync -avm \
	--chown=0:0 \
	--include="*/" \
	--include="/arch/${ARCH}/include/**" \
	--include="/arch/${ARCH}/mach-zynq/include/**" \
	--include="/include/**" \
	--include="/scripts/**" \
	--exclude="*" \
	. $kerneldevdir

	cd ${KBUILD_OUTPUT}

	# Copy generated headers and build objects
	rsync -avm \
	--chown=0:0 \
	--include="*/" \
	--exclude="*.o"  \
	--exclude=".debug" \
	--exclude=".*.cmd" \
	--include="/arch/${ARCH}/include/generated/**" \
	--include="/include/**" \
	--include="/scripts/**${CROSS_COMPILE}*" \
	--include="/scripts/**.h" \
	--include=".config" \
	--include="Module.symvers" \
	--exclude="*" \
	. $kerneldevdir

	# Strip cross compile prefix from target-specific binaries
	for u in recordmcount fixdep modpost genksyms; do
		find $kerneldevdir/scripts -name "${CROSS_COMPILE}$u" -printf %h | \
		xargs -n 1 -I '{}' mv -f '{}'"/${CROSS_COMPILE}$u" '{}'"/$u"
	done

	# Remove VC-specific files
	for f in $(find $kerneldevdir -name .gitignore); do
		rm $f;
	done
}

do_tag_kernel_build() {
	if [ -z "${BUILD_IDENTIFIER}" -o "${ENABLE_BUILD_TAG_PUSH}" != "Yes" ]; then
		bbnote "Skipping git repo tagging (" $([ -z "${BUILD_IDENTIFIER}" ] && echo "no BUILD_IDENTIFIER defined" || echo "tagging not enabled by ENABLE_BUILD_TAG_PUSH") ")"
		return 0
	fi

	machine_branch="${@ get_machine_branch(d, "${KBRANCH}" )}"
	pushurl="${NILRT_RW_GIT_URI}:${GIT_KERNEL_REPO}"
	sshurl="${pushurl%:*}"

	if [ -z "${sshurl}" -o -z "${machine_branch}" ]; then
		bbfatal "Issue with push URL \"${pushurl}\" or branch \"${machine_branch}\""
	fi

	cd ${S}

	git tag -f ${BUILD_IDENTIFIER} ${machine_branch}

# We'll let this fail if the same tag already exists on the server (something's
# gone terribly wrong or, more likely, a minor oversight.)
	git push ${pushurl} ${BUILD_IDENTIFIER}
}

addtask tag_kernel_build after do_compile before do_install
