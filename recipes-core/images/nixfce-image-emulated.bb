FILESPATH = "${@base_set_filespath([ '${FILE_DIRNAME}/niconsole-image-emulated' ], d)}"

require niconsole-image-emulated.bb

IMAGE_FEATURES += "x11-base"

IMAGE_INSTALL += "packagegroup-ni-xfce"
