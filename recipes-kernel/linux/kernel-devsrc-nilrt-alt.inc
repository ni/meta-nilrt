# This includes file contains content which is only appropriate for *alternate*
# (non-standard) versions of the kernl-devsrc package (eg. kernel-devsrc-next).

do_install_append() {
   # Remove the symlink so it doesn't conflict with other
   # kernel-devsrc packages
   rm -f ${D}/usr/src/kernel
}

