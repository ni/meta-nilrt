# (C) Copyright 2013,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "Xfce desktop environment packages for NI Linux Realtime distribution"
LICENSE = "MIT"


PACKAGE_ARCH = "${MACHINE_ARCH}"


inherit packagegroup


RDEPENDS:${PN} = "packagegroup-xfce-base"

RDEPENDS:${PN} += "\
	font-cursor-misc \
	font-misc-misc \
	fontconfig-overrides \
	mousepad \
	ttf-pt-sans \
	xf86-input-evdev \
	xfce-nilrt-settings \
	xfce4-xkb-plugin \
	xfontsel \
	xorg-fonts-100dpi \
	xrdb \
	xserver-xfce-init \
"

RDEPENDS:${PN}:append:x64 += "\
	xf86-video-ati \
	xf86-video-intel \
	xf86-video-vesa \
"
