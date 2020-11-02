DESCRIPTION = "Tiny initramfs image with rauc bundle intended to run restore mode operations"
NIBOOT_BUNDLE_IMAGE = "nilrt-base-bundle"
IMAGE_DISPLAY_NAME = "NI LinuxRT Base System Image"

IMAGE_BASENAME = "nilrt-recovery-media"
WKS_FILE = "${IMAGE_BASENAME}.${MACHINE}.wks.in"

IMAGE_FSTYPES_append_x64 = " wic"

do_rootfs[depends] += "${NIBOOT_BUNDLE_IMAGE}:do_deploy"

PAYLOAD_DIR = "${T}/payload"

install_payload () {
   rm -rf ${PAYLOAD_DIR}
   install -d ${PAYLOAD_DIR}/payload
   install -m 0644 ${DEPLOY_DIR_IMAGE}/${NIBOOT_BUNDLE_IMAGE}-${MACHINE}.raucb  ${PAYLOAD_DIR}/payload/niboot.raucb
   echo "BUILD_IDENTIFIER=${BUILDNAME}" > ${PAYLOAD_DIR}/payload/imageinfo
   echo "IMAGE_DISPLAY_NAME=${IMAGE_DISPLAY_NAME}" >> ${PAYLOAD_DIR}/payload/imageinfo
}

ROOTFS_POSTPROCESS_COMMAND += "install_payload;"

require nilrt-restore-mode.inc
