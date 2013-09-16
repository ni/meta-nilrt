FILESPATH = "${@base_set_filespath([ '${FILE_DIRNAME}/niconsole-image-emulated' ], d)}"

require niconsole-image-emulated.bb

IMAGE_FEATURES += "x11-base"

IMAGE_INSTALL += " \
	avahi-daemon \
	avahi-dnsconfd \
	libavahi-client \
	libavahi-common \
	libavahi-core \
	packagegroup-xfce-base \
	xf86-video-vesa \
	xfce4-xkb-plugin \
	xserver-xfce-init \
"

