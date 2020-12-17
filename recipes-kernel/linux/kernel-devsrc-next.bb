KERNEL_DEPENDENCY = "linux-nilrt-next"
KERNEL_DIRECTORY = "${DEPLOY_DIR_IMAGE}/kernel-next/staging_kernel_dir"
KERNEL_BUILD_DIRECTORY = "${DEPLOY_DIR_IMAGE}/kernel-next/staging_kernel_builddir"

require recipes-kernel/linux/kernel-devsrc.inc

do_install_append() {
   # Remove the symlink so it doesn't conflict with other
   # kernel-devsrc packages
   rm -f ${D}/usr/src/kernel
}

pkg_postinst_ontarget_${PN} () {
   cd /lib/modules/${KERNEL_VERSION}/build
   make prepare
   make modules_prepare

   if [ -e /etc/kernel/postinst.d ]; then
      run-parts /etc/kernel/postinst.d -a ${KERNEL_VERSION}
   fi
}

pkg_prerm_${PN} () {
   if [ -e /etc/kernel/prerm.d ]; then
      run-parts /etc/kernel/prerm.d -a ${KERNEL_VERSION}
   fi
}
