require nilrt-u-boot.inc

SRC_URI = "${NILRT_GIT}/u-boot.git;protocol=git;branch=${UBOOT_BRANCH}"
SRCREV = "${AUTOREV}"
