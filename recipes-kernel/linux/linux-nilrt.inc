inherit kernel
require recipes-kernel/linux/linux-yocto.inc

machine_srcrev="${SRCREV}"

EXTRA_OEMAKE += 'CFLAGS="${TOOLCHAIN_OPTIONS}"'

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
