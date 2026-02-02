SUMMARY = "Clevis - Automated Encryption Framework"
DESCRIPTION = "Clevis is a pluggable framework for automated decryption. It \
can be used to provide automated decryption of data or even automated \
unlocking of LUKS volumes."
HOMEPAGE = "https://github.com/latchset/clevis"
SECTION = "security"
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = "\
	file://COPYING;md5=d32239bcb673463ab874e80d47fae504 \
	file://COPYING.openssl;md5=a78c00d154a43f35ef1dc1292a234c6d \
"


DEPENDS = "\
	cryptsetup \
	cryptsetup-native \
	jansson \
	jose \
	keyutils-native \
"

SRC_URI = "\
	https://github.com/latchset/clevis/releases/download/v${PV}/${BP}.tar.xz \
"
SRC_URI[sha256sum] = "a0388a544c77139dc751cdbf66bdd38fc29c43f9e81a1cdfd119c84109ffca3f"


# ==============================================================================
# BBCLASSES
# ==============================================================================

# CONFIGURATION AND BUILD
inherit meson pkgconfig

PACKAGECONFIG ??= ""
PACKAGECONFIG[docs] = ",, asciidoc-native"
PACKAGECONFIG[dracut] = ",, dracut, dracut"
PACKAGECONFIG[luks] = ",, luksmeta, cryptsetup jq"
PACKAGECONFIG[pkcs11] = ",, opensc-native, opensc"
PACKAGECONFIG[tpm2] = ",, tpm2-tools-native, tpm2-tools"
# TODO: Add support for systemd systems.
# initramfs-tools integration intentionally skipped due to no-support in OE.


inherit bash-completion


# PTESTING

inherit ptest

do_install_ptest () {
	install -d ${D}${PTEST_PATH}
	install -m 0744 ${S}/src/luks/tests/* ${D}${PTEST_PATH}
	# TODO: more tests
}

RDEPENDS:${PN}-ptest += " bash cryptsetup"
RRECOMMENDS:${PN}-ptest += " jq keyutils"


# ==============================================================================
# PACKAGING
# ==============================================================================
# clevis
FILES:${PN} += " ${libdir}/dracut/*"
RDEPENDS:${PN} += " bash tpm2-tools"
