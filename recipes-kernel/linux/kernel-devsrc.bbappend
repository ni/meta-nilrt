pkg_postinst_ontarget_kernel-devsrc () {
   cd /lib/modules/${KERNEL_VERSION}/build/scripts/mod
   make prepare
   make modules_prepare
}
