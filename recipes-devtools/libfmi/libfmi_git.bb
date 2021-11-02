DESCRIPTION="FMI Library (FMIL) is a software package written in C that enables integration of Functional Mock-up Units (FMUs) import in applications."
HOMEPAGE = "https://github.com/svn2github/FMILibrary"
SECTION = "libs"

SRC_URI = "git://github.com/svn2github/FMILibrary.git;protocol=https"
SRCREV = "d49ed3ff2dabc6e17cc4a0c6f3fa6d2ae64a1683"

S = "${WORKDIR}/git"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=feb42903281464837bc0c9a861b1e7a1"

inherit cmake

EXTRA_OECMAKE = "\
	-DFMILIB_BUILD_TESTS=OFF \
	-DFMILIB_GENERATE_DOXYGEN_DOC=OFF \
	-DFMILIB_INSTALL_PREFIX=${prefix} \
"

RDEPENDS_${PN}-dev = ""

FILES_${PN}-doc += "/usr/doc"

INSANE_SKIP_${PN}-dev = "dev-elf"
