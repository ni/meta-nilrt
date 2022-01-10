# (C) Copyright 2013,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "Xfce desktop environment packages for NI Linux Realtime distribution"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} = "\
	packagegroup-xfce-base \
	xf86-input-evdev \
	xfce4-xkb-plugin \
	xserver-xfce-init \
	xrdb \
	xfce-nilrt-settings \
	font-cursor-misc \
	font-misc-misc \
	xorg-fonts-100dpi \
	xfontsel \
	fontconfig-overrides \
	mousepad \
	ttf-pt-sans \
	xserver-xorg-udev-rules \
	onboard \
"
RDEPENDS_${PN}_append_x64 += "\
	xf86-video-ati \
	xf86-video-intel \
	xf86-video-vesa \
"