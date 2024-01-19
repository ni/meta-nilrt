SUMMARY = "UTF-8 with C++ in a Portable Way"
DESCRIPTION = "\
C++ developers still miss an easy and portable way of handling Unicode encoded \
strings. The original C++ standard (known as C++98 or C++03) is Unicode \
agnostic. Some progress has been made in the later editions of the standard, \
but it is still hard to work with Unicode using only the standard facilities.\
\
I came up with a small, C++98 compatible generic library in order to \
handle UTF-8 encoded strings. For anybody used to work with STL algorithms and \
iterators, it should be easy and natural to use. The code is freely available \
for any purpose - check out the license. The library has been used a lot since \
the first release in 2006 both in commercial and open-source projects and \
proved to be stable and useful."
HOMEPAGE = "https://github.com/nemtrif/utfcpp"
SECTION = "libs"
LICENSE = "BSL-1.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=e4224ccaecb14d942c71d31bef20d78c"

DEPENDS = ""

SRC_URI = "\
	git://github.com/nemtrif/utfcpp;branch=master;protocol=https \
"
SRCREV = "6be08bbea14ffa0a5c594257fb6285a054395cd7"

S = "${WORKDIR}/git"


inherit cmake


# For a library, the header files should be in the base package. The cmake
# configs should be in -dev.
FILES:${PN}:append = " ${includedir}"
FILES:${PN}-dev:remove = "${includedir}"
FILES:${PN}-dev:append = " ${datadir}/${BPN}/cmake"

RDEPENDS:${PN}-dev:append = "${PN}"

BBCLASSEXTEND = "native"
