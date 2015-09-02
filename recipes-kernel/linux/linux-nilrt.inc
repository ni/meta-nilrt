inherit kernel
require recipes-kernel/linux/linux-yocto.inc

machine_srcrev="${SRCREV}"

EXTRA_OEMAKE += 'CFLAGS="${TOOLCHAIN_OPTIONS}"'

# No need to do debug symbol handling for kernel builds; also the default behavior
# incorrectly splits out some 'kernel-dev' files under /lib/modules/<ver>/build
INHIBIT_PACKAGE_DEBUG_SPLIT="1"

# The NI kernel build includes on-target versioning tools that
# link against the gcc provided runtime
do_kernel_configme[depends] += "libgcc:do_populate_sysroot"

SRC_URI = "git://git.amer.corp.natinst.com/linux.git;protocol=git;nocheckout=1;branch=${KBRANCH}"
SRCREV="${AUTOREV}"
PV = "${LINUX_VERSION}+git${SRCPV}"

FILESEXTRAPATHS_prepend := "${THISDIR}/${KBUILD_FRAGMENTS_LOCATION}:"

KBUILD_DEFCONFIG_x64 = "nati_x86_64_defconfig"
KBUILD_DEFCONFIG_xilinx-zynq = "nati_zynq_defconfig"
KCONFIG_MODE="--alldefconfig"

# Install headers and build objects for kernel versioning into
# /lib/modules/${KERNEL_VERSION}/build for inclusion in kernel-dev.
do_install_append() {
	kerneldevdir=${D}/lib/modules/${KERNEL_VERSION}/build
	install -d $kerneldevdir

	cd ${S}

	# Copy Makefile, Kconfig files
	rsync -avm \
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
	--include="*/" \
	--exclude="*.o"  \
	--exclude=".debug" \
	--exclude=".*.cmd" \
	--include="/arch/${ARCH}/include/generated/**" \
	--include="/include/**" \
	--include="/scripts/**" \
	--include=".config" \
	--include="Module.symvers" \
	--exclude="*" \
	. $kerneldevdir
}
