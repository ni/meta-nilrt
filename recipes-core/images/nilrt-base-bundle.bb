DESCRIPTION = "NILRT base image RAUC bundle"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

BUNDLE_IMAGE = "nilrt-base-bundle-image"

DEPENDS = "${BUNDLE_IMAGE}"

LICENSE_CREATE_PACKAGE = "0"

SRC_URI += " \
    file://nilrt-bundle-hooks.sh \
"

RAUC_BUNDLE_COMPATIBLE = "nilrt-efi-ab"
RAUC_BUNDLE_DESCRIPTION = "${DESCRIPTION}"
RAUC_BUNDLE_VERSION = "${BUILDNAME}"
RAUC_BUNDLE_BUILD = "${BUILDNAME}"

RAUC_BUNDLE_HOOKS = "1"
RAUC_BUNDLE_HOOKS[file] = "nilrt-bundle-hooks.sh"
RAUC_BUNDLE_HOOKS[hooks] = "install-check;"

RAUC_BUNDLE_SLOTS = "niboot"
RAUC_SLOT_niboot = "${BUNDLE_IMAGE}"
RAUC_SLOT_niboot[fstype] = "tar.bz2"
RAUC_SLOT_niboot[hooks] = "pre-install;post-install;"

RAUC_SIGN_BUNDLE = "0"

inherit bundle
