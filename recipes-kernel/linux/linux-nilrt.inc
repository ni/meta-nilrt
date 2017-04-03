inherit kernel
require recipes-kernel/linux/linux-yocto.inc

machine_srcrev="${SRCREV}"

# Setting EXTRA_OEMAKE to include CFLAGS settings is required as
# the Kbuild system will clobber CC (which is used by OE for setting
# the compiler and flags to the compiler/linker/etc.)
EXTRA_OEMAKE += 'CFLAGS="${TOOLCHAIN_OPTIONS}"'

# The default -hf arm tools don't default to -mfloat-abi=hard, and this
# causes build failures when building the scripts utilities
TOOLCHAIN_OPTIONS_append_xilinx-zynqhf = " -mfloat-abi=hard "
EXTRA_OEMAKE_xilinx-zynqhf += 'CFLAGS="${CFLAGS} ${TOOLCHAIN_OPTIONS}"'

# No need to do debug symbol handling for kernel builds; also the default behavior
# incorrectly splits out some 'kernel-dev' files under /lib/modules/<ver>/build
INHIBIT_PACKAGE_DEBUG_SPLIT="1"

# The NI kernel build includes on-target versioning tools that
# link against the gcc provided runtime
do_kernel_configme[depends] += "libgcc:do_populate_sysroot"

SRC_URI = "${NILRT_GIT}/linux.git;protocol=git;nocheckout=1;branch=${KBRANCH}"
SRCREV="${AUTOREV}"
PV = "${LINUX_VERSION}+git${SRCPV}"

FILESEXTRAPATHS_prepend := "${THISDIR}/${KBUILD_FRAGMENTS_LOCATION}:"

KBUILD_DEFCONFIG_x64 = "nati_x86_64_defconfig"
KBUILD_DEFCONFIG_xilinx-zynq = "nati_zynq_defconfig"
KBUILD_DEFCONFIG_xilinx-zynqhf = "nati_zynq_defconfig"
KCONFIG_MODE="--alldefconfig"

#create dtb-s fromt dts files for xilinx-zynqhf targets
DEPENDS_xilinx-zynqhf += "dtc-native"
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
        ni-7885.dtb "

#create uImage for xilinx-zynqhf targets
KERNEL_IMAGETYPE_xilinx-zynqhf = "uImage"
UBOOT_LOADADDRESS_xilinx-zynqhf ?= "0x8000"
UBOOT_ENTRYPOINT_xilinx-zynqhf ?= "${UBOOT_LOADADDRESS}"
KERNEL_EXTRA_ARGS_xilinx-zynqhf += "LOADADDR=0x8000"

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
