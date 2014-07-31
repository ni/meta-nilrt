
inherit core-image

# without package-management update-rc.d gets removed from image
IMAGE_FEATURES += "package-management"
