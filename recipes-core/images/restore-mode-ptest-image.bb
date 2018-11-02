DESCRIPTION = "Initramfs image intended to install only the minimal-nilrt-ptest-image"
FACTORY_IMAGE = "minimal-nilrt-ptest-image"
IMAGE_DISPLAY_NAME = "ptest runmode"

# image.bbclass checks the resulting initramfs size and the default of 128MB 
# is too small for this image.  The hardware targets for this image will have
# a minimum of 4GB of RAM, and OE advises this value to be less than half of
# available RAM.
INITRAMFS_MAXSIZE = "524288"

require restore-mode-image.inc
