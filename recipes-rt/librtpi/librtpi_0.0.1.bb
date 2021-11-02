SUMMARY = "Real-Time Priority Inheritance C Library Wrapper"
HOMEPAGE = "https://github.com/dvhart/librtpi"
SECTION = "devel/libs"
DEPENDS = ""
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1803fa9c2c3ce8cb06b4861d75310742"

SRC_URI = "\
	git://github.com/gratian/librtpi.git;protocol=https;branch=ni/latest \
	file://librtpi-use-serial-tests-config-needed-by-ptest.patch \
	file://run-ptest \
"

PV = "0.0.1+git${SRCPV}"
SRCREV="558653f9fd48ec755d8feca7bce3cd7824018d9b"

S = "${WORKDIR}/git"

inherit autotools ptest

FILES_${PN}-dev += "${libdir}/*.so ${includedir}/*.h"
FILES_${PN}-staticdev += "${libdir}/*.a"

LDFLAGS += "-lpthread"

RDEPENDS_${PN}-ptest += "make"

do_compile_ptest() {
    oe_runmake -C tests buildtest-TESTS
    oe_runmake -C tests/glibc-tests buildtest-TESTS
}

do_install_ptest () {
    # install librtpi native tests
    install -d ${D}${PTEST_PATH}/tests
    install -m 644 ${B}/tests/Makefile ${D}${PTEST_PATH}/tests
    install -m 0755 ${B}/tests/.libs/test_api ${D}${PTEST_PATH}/tests
    find ${B}/tests/.libs -maxdepth 1 -name 'tst-cond*' -type f -perm -111 -exec \
        install -m 0755 {} ${D}${PTEST_PATH}/tests \;

    # install librtpi imported glibc tests
    install -d ${D}${PTEST_PATH}/tests/glibc-tests
    install -m 644 ${B}/tests/glibc-tests/Makefile ${D}${PTEST_PATH}/tests/glibc-tests
    find ${B}/tests/glibc-tests/.libs -maxdepth 1 -name 'tst-cond*' -type f -perm -111 -exec \
        install -m 0755 {} ${D}${PTEST_PATH}/tests/glibc-tests \;

    # remove dependencies for re-building the Makefile(s) themselves
    # these are only needed at build time and cause errors at test run-time
    sed -i 's/^Makefile:.*$/Makefile:/' ${D}${PTEST_PATH}/tests/Makefile
    sed -i 's/^Makefile:.*$/Makefile:/' ${D}${PTEST_PATH}/tests/glibc-tests/Makefile

    # remove tst-condpi2.sh test; it doesn't play well with the split builttest/runtest targets
    sed -i 's/tst-condpi2\.sh//' ${D}${PTEST_PATH}/tests/Makefile
}
