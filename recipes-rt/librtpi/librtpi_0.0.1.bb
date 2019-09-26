SUMMARY = "Real-Time Priority Inheritance C Library Wrapper"
HOMEPAGE = "https://github.com/dvhart/librtpi"
SECTION = "devel/libs"
DEPENDS = ""
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1803fa9c2c3ce8cb06b4861d75310742"

SRC_URI = "${NILRT_GIT}/librtpi.git;branch=gratian/latest"

PV = "0.0.1+git${SRCPV}"
SRCREV="558653f9fd48ec755d8feca7bce3cd7824018d9b"

S = "${WORKDIR}/git"

inherit autotools

FILES_${PN}-dev += "${libdir}/*.so ${includedir}/*.h"
FILES_${PN}-staticdev += "${libdir}/*.a"

LDFLAGS += "-lpthread"
