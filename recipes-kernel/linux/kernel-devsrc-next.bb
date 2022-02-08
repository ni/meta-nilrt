KERNEL_DEPENDENCY = "linux-nilrt-next"
KERNEL_DIRECTORY = "${DEPLOY_DIR_IMAGE}/kernel-next/staging_kernel_dir"
KERNEL_BUILD_DIRECTORY = "${DEPLOY_DIR_IMAGE}/kernel-next/staging_kernel_builddir"

require recipes-kernel/linux/kernel-devsrc.bb
require kernel-devsrc-nilrt.inc
require kernel-devsrc-nilrt-alternate.inc
