DESCRIPTION = "Extensible Modelica-based platform for optimization, simulation and analysis of complex dynamic systems."
HOMEPAGE = "https://jmodelica.org/"
SECTION = "libs"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=feb42903281464837bc0c9a861b1e7a1"

SRC_URI = "git://github.com/modelon-community/fmi-library;protocol=https;branch=master \
           file://0001-Remove-expat-static-linking.patch \
           "

SRCREV = "29523c20aec17277fc517900e3506d17d3f64642"

S = "${WORKDIR}/git"

DEPENDS = "expat"

inherit cmake pkgconfig

EXTRA_OECMAKE = "\
	-DFMILIB_BUILD_TESTS=OFF \
	-DFMILIB_GENERATE_DOXYGEN_DOC=OFF \
	-DFMILIB_INSTALL_PREFIX=${prefix} \
	-DFMILIB_BUILD_STATIC_LIB=OFF \
"

RDEPENDS:${PN}-dev = ""

FILES:${PN}-doc += "/usr/doc"

INSANE_SKIP:${PN}-dev = "dev-elf"
