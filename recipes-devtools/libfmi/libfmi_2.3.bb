DESCRIPTION = "Extensible Modelica-based platform for optimization, simulation and analysis of complex dynamic systems."
HOMEPAGE = "https://jmodelica.org/"
SECTION = "libs"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=feb42903281464837bc0c9a861b1e7a1"

SRC_URI = "git://github.com/modelon-community/fmi-library;protocol=https \
           file://0001-fmixml-use-system-expat-instead-of-building-own-with.patch \
           "

SRCREV = "917964fa0a6b30c4f2acc62b5f04df1ecae07c68"

S = "${WORKDIR}/git"

DEPENDS = "expat"

inherit cmake pkgconfig

EXTRA_OECMAKE = "\
	-DFMILIB_BUILD_TESTS=OFF \
	-DFMILIB_GENERATE_DOXYGEN_DOC=OFF \
	-DFMILIB_INSTALL_PREFIX=${prefix} \
	-DFMILIB_BUILD_STATIC_LIB=OFF \
"

RDEPENDS_${PN}-dev = ""

FILES_${PN}-doc += "/usr/doc"

INSANE_SKIP_${PN}-dev = "dev-elf"
