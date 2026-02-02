SUMMARY = "Jose - C-language implementation of Javascript Object Signing and \
Encryption"
DESCRIPTION = "José is a C-language implementation of the Javascript Object \
Signing and Encryption standards. Specifically. José is extensively tested \
against the RFC test vectors."
HOMEPAGE = "https://github.com/latchset/jose"
SECTION = "security"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "\
	file://COPYING;md5=34400b68072d710fecd0a2940a0d1658 \
"

DEPENDS = "\
	openssl \
	jansson \
	zlib \
"


SRC_URI = "\
	https://github.com/latchset/jose/releases/download/v14/jose-14.tar.xz \
"
SRC_URI[sha256sum] = "cee329ef9fce97c4c025604a8d237092f619aaa9f6d35fdf9d8c9052bc1ff95b"


# ==============================================================================
# BBCLASSES
# ==============================================================================

inherit meson pkgconfig


BBCLASSEXTEND = "native"
