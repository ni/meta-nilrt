pkg_postinst_ontarget_kernel-devsrc () {
   cd /lib/modules/${KERNEL_VERSION}/build
   make prepare
   make modules_prepare
}
