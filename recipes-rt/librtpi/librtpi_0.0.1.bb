SUMMARY = "Real-Time Priority Inheritance C Library Wrapper"
HOMEPAGE = "https://github.com/dvhart/librtpi"
SECTION = "devel/libs"
DEPENDS = ""
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1803fa9c2c3ce8cb06b4861d75310742"

SRC_URI = "git://github.com/dvhart/librtpi.git"

PV = "0.0.1+git${SRCPV}"
SRCREV="37a96aa7da7d9873b982e84d57b150423c0a0e60"

S = "${WORKDIR}/git"

inherit autotools

FILES_${PN}-dev += "${libdir}/*.so ${includedir}/*.h"
FILES_${PN}-staticdev += "${libdir}/*.a"

LDFLAGS += "-lpthread"
