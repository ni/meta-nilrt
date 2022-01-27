SUMMARY = "mkinitcpio is the next generation of initramfs creation."
DESCRIPTION = "mkinitcpio is a modular tool for building an initramfs CPIO image, offering many advantages over alternative methods."
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://LICENSE;md5=eb723b61539feef013de476e68b5c50a"

SRCREV = "37df4fe219b5bdf8373d8a3c9c6145c122bb494b"
BRANCH="nilrt/18.0"
PV = "v23+git${SRCPV}"

SRC_URI = "\
	${NILRT_GIT}/mkinitcpio.git;protocol=https;branch=${BRANCH} \
	file://0001-Makefile-don-t-check-asciidoc-output.patch \
	file://0002-Makefile-don-t-preserve-ownership-on-install.patch \
"

S = "${WORKDIR}/git"

RDEPENDS_${PN} += "bash"

DEPENDS += "asciidoc-native libxml2-native xmlto-native"

inherit autotools-brokensep systemd

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = "mkinitcpio-generate-shutdown-ramfs.service"
SYSTEMD_AUTO_ENABLE = "enable"

FILES_${PN} = "${bindir} ${sysconfdir} ${libdir} /usr/share"
