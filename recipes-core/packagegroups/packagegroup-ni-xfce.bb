# (C) Copyright 2013,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "Xfce desktop environment packages for NI Linux Realtime distribution"
LICENSE = "MIT"
PR = "r1"

inherit packagegroup

PACKAGE_ARCH = "${MACHINE_ARCH}"

RDEPENDS_${PN} = "\
	packagegroup-xfce-base \
	xf86-video-vesa \
	xfce4-xkb-plugin \
	xserver-xfce-init \
	xrdb \
	xfce-nilrt-settings \
	xorg-fonts-misc \
	xorg-fonts-100dpi \
	xfontsel \
	fontconfig-overrides \
"
