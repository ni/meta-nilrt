DESCRIPTION = "Tiny initramfs image intended to run restore mode operations"
NIBOOT_BUNDLE_IMAGE = "nilrt-base-bundle"
IMAGE_DISPLAY_NAME = "NI LinuxRT Base System Image"

IMAGE_FSTYPES_append_x64 = " wic"

require nilrt-restore-mode.inc
