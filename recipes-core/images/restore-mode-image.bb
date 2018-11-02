DESCRIPTION = "Tiny initramfs image intended to run restore mode operations, uses minimal-nilrt-image"
FACTORY_IMAGE = "minimal-nilrt-image"
IMAGE_DISPLAY_NAME = "minimal runmode"

IMAGE_FSTYPES_append_x64 = " wic"

require restore-mode-image.inc
