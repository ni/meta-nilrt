# Rules for image recipes which: contain a valid kernel and init (PID 1); are
# packed as a CPIO archive; and expected to provide an initial ramfs for
# booting a later, complete rootfs.

IMAGE_FSTYPES += " ${INITRAMFS_FSTYPES}"

BAD_RECOMMENDATIONS_append_pn-${PN} += "\
	ca-certificates \
	shared-mime-info \
"
