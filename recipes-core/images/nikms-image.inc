# This image uses the NI Kernel Management System instead of DKMS

IMAGE_INSTALL += "\
	ni-module-versioning-tools.ipk \
	"

# older NILRT modules are versioned using two squashfs images, one with headers,
# the other with tools such as gcc, make, etc. NXG images don't use these.
do_rootfs[depends] += "\
	module-tools:do_image_complete \
	module-headers:do_image_complete \
	"

# rootfs destination names don't make much sense but keep them to avoid breaking
# backwards compatibility on older NILRT (tools are expecting these paths).
install_module_versioning_squashfs() {
	TOOLS_IMAGE="${PREFERRED_PROVIDER_virtual/kernel}-module-tools"
	HEADERS_IMAGE="${PREFERRED_PROVIDER_virtual/kernel}-module-headers"

	install -d ${IMAGE_ROOTFS}/usr/local/natinst/tools

	install -m 0644 ${DEPLOY_DIR_IMAGE}/${TOOLS_IMAGE}-${MACHINE}.squashfs \
		${IMAGE_ROOTFS}/usr/local/natinst/tools/tools-squashfs-image.squashfs
	install -m 0644 ${DEPLOY_DIR_IMAGE}/${HEADERS_IMAGE}-${MACHINE}.squashfs \
		${IMAGE_ROOTFS}/usr/local/natinst/tools/module-versioning-image.squashfs
}

ROOTFS_POSTPROCESS_COMMAND += "install_module_versioning_squashfs;"
