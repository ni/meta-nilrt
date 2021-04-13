# Additional rules for extra kernels (other than the main virtual/kernel)

KERNEL_PACKAGE_NAME_append = "-${LINUX_KERNEL_TYPE}"
KBUILD_FRAGMENTS_LOCATION = "nilrt-${LINUX_KERNEL_TYPE}"

require linux-nilrt.inc

# Force creation of symlink to target file at a relative path
KERNEL_IMAGE_SYMLINK_DEST = "."

# Building kernel-devsrc for additional kernels requires access to
# staging and build dirs.
kernel_do_deploy_append() {
    cp -rf ${STAGING_KERNEL_DIR} $deployDir/staging_kernel_dir
    cp -rf ${STAGING_KERNEL_BUILDDIR} $deployDir/staging_kernel_builddir
    rm -f $deployDir/staging_kernel_builddir/source
}

LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"
