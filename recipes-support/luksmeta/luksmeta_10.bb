SUMMARY = "LUKSMeta"
DESCRIPTION = "Welcome to LUKSMeta! LUKSMeta is a simple library for storing \
metadata in the LUKSv1 header. This library is licensed under the GNU LGPLv2+."
HOMEPAGE = "https://github.com/latchset/luksmeta"
SECTION = "security"
LICENSE = "LGPL-2.1-or-later"
LIC_FILES_CHKSUM = "\
	file://COPYING;md5=4e9dfcb21c14eb0c40ae8ba436d3bb7a \
"

DEPENDS = "\
    cryptsetup \
"


SRC_URI = "\
    https://github.com/latchset/luksmeta/releases/download/v10/luksmeta-10.tar.bz2 \
"
SRC_URI[sha256sum] = "a842538ba39680c8319c41dac0bcc082fe40fb43342561761925c0daa1a48f28"


inherit autotools pkgconfig

# ==============================================================================
# PACKAGING
# ==============================================================================


BBCLASSEXTEND = "native"
