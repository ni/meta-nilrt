DESCRIPTION = "Tiny initramfs image intended to run restore mode operations"
IMAGE_DISPLAY_NAME = "NI LinuxRT Base System Image"

IMAGE_BASENAME = "nilrt-restore-mode"
WKS_FILE = "${IMAGE_BASENAME}.${MACHINE}.wks"

IMAGE_FSTYPES_append_x64 = " wic"

require nilrt-restore-mode.inc
