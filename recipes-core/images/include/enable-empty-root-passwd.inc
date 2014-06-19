# set root password
# use the password in the shadow file
ROOTFS_POSTPROCESS_COMMAND += "sed -i -e's/root::/root:x:/' ${IMAGE_ROOTFS}/etc/passwd; "
IMAGE_FEATURES += "empty-root-password"
