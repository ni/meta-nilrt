# set root password
# use the password in the shadow file
ROOTFS_POSTPROCESS_COMMAND += "shadowify_passwd_for_root; "
IMAGE_FEATURES += "empty-root-password"

shadowify_passwd_for_root() {
	sed -i -e's/root::/root:x:/' ${IMAGE_ROOTFS}/etc/passwd
}
