KERNEL_DEPENDENCY = "linux-nilrt-nohz"
KERNEL_DIRECTORY = "${DEPLOY_DIR_IMAGE}/kernel-nohz/staging_kernel_dir"
KERNEL_BUILD_DIRECTORY = "${DEPLOY_DIR_IMAGE}/kernel-nohz/staging_kernel_builddir"

require recipes-kernel/linux/kernel-devsrc.inc
require kernel-devsrc-nilrt.inc
require kernel-devsrc-nilrt-alternate.inc
