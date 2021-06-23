SUMMARY = "Linux kernel NO_HZ_FULL polling test"
HOMEPAGE = "https://kernel.org"
SECTION = "tests"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://nohz_test.c;md5=d25cf78b2ec478f88868ccbc1967e9da"

inherit ptest

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-files:"

S = "${WORKDIR}"

RDEPENDS_${PN}-ptest += "bash procps rt-tests"
RDEPENDS_${PN}-ptest_append_x64 += "packagegroup-ni-nohz-kernel"
ALLOW_EMPTY_${PN} = "1"

SRC_URI += "\
    file://run-ptest \
    file://nohz_test.c \
"

LDFLAGS += "-lpthread"

do_compile_ptest_append() {
    cd ${WORKDIR}
    ${CC} ${CFLAGS} -o nohz_test nohz_test.c ${LDFLAGS}
}

do_install_ptest_append() {
    install -m 0755 ${S}/run-ptest ${D}${PTEST_PATH}
    install -m 0755 ${S}/nohz_test ${D}${PTEST_PATH}
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
