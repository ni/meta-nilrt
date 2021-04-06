NI_RELEASE_VERSION = "master"
LINUX_VERSION = "5.10"
KBRANCH = "nilrt/${NI_RELEASE_VERSION}/${LINUX_VERSION}"
GIT_KERNEL_REPO = "linux.git"

require linux-nilrt.inc

# This is the place to overwrite the source AUTOREV from linux-nilrt.inc, if
# the kernel recipe requires a particular ref.
#SRCREV = ""

kernel_do_deploy_append() {
    cp -rf ${STAGING_KERNEL_DIR} $deployDir/staging_kernel_dir
    cp -rf ${STAGING_KERNEL_BUILDDIR} $deployDir/staging_kernel_builddir
    rm -f $deployDir/staging_kernel_builddir/source
}

LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"
