# set root password
# password is encrypted, use "cryptpw -a des <clear_text_password>" to get encrypted value
ROOTFS_POSTPROCESS_COMMAND += "sed -i -e's/root::/root:NP:/' ${IMAGE_ROOTFS}/etc/shadow; "

# mark hold packages
ROOTFS_POSTPROCESS_COMMAND += "opkg-cl ${IPKG_ARGS} flag hold libc6; "

inherit core-image

# without package-management update-rc.d gets removed from image
IMAGE_FEATURES += "package-management"
