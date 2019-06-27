DESCRIPTION = "Initramfs image intended to install only the lvcomms-nilrt-image"
NIBOOT_BUNDLE_IMAGE = "lvcomms-nilrt-bundle"
IMAGE_DISPLAY_NAME = "lvcomms runmode"

# image.bbclass checks the resulting initramfs size and the default of 128MB 
# is too small for this image.  The hardware targets for this image will have
# a minimum of 4GB of RAM, and OE advises this value to be less than half of
# available RAM.
INITRAMFS_MAXSIZE = "524288"
IMAGE_FSTYPES_append_x64 = " wic"

require restore-mode-image.inc
