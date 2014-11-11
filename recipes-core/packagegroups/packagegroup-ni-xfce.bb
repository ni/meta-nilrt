# (C) Copyright 2013,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "Xfce desktop environment packages for NI Linux Realtime distribution"
LICENSE = "MIT"
PR = "r1"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} = "\
	packagegroup-xfce-base \
	xf86-video-vesa \
	xfce4-xkb-plugin \
	xserver-xfce-init \
	xrdb \
	xfce-nilrt-settings \
	font-cursor-misc \
	font-misc-misc \
	xorg-fonts-100dpi \
	xfontsel \
	fontconfig-overrides \
	pt-sans \
	gnome-icon-theme-minimal \
	mousepad \
"
