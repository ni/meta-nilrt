# Copyright (C) 2026 National Instruments
# Released under the MIT license (see COPYING.MIT for the terms)

SUMMARY = "NI Wireless common scripts"
HOMEPAGE = "http://www.ni.com"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

WIRELESS_COMMON:x64 = "wireless.common.x64"
WIRELESS_COMMON:xilinx-zynq = "wireless.common.arm"

SRC_URI = "file://${WIRELESS_COMMON}"

S = "${WORKDIR}"

do_install() {
	install -d ${D}${sysconfdir}/natinst/networking
	install -m 0644 ${S}/${WIRELESS_COMMON} ${D}${sysconfdir}/natinst/networking/wireless.common
}
