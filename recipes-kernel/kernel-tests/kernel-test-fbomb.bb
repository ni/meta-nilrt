SUMMARY = "Linux kernel futex test"
HOMEPAGE = "https://kernel.org"
SECTION = "tests"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://fbomb.c;md5=6ca6ccab92c415e517d1b65819dbd273"

inherit ptest

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-files:"

S = "${WORKDIR}"

DEPENDS = "virtual/kernel"
RDEPENDS_${PN}-ptest += "bash kmod"

ALLOW_EMPTY_${PN} = "1"

SRC_URI += "\
    file://run-ptest \
    file://fbomb.c \
"

LDFLAGS += "-lpthread"

do_compile_ptest_append() {
    cd ${WORKDIR}
    ${CC} ${CFLAGS} -o fbomb fbomb.c ${LDFLAGS}
}

do_install_ptest_append() {
    install -m 0755 ${S}/run-ptest ${D}${PTEST_PATH}
    install -m 0755 ${S}/fbomb ${D}${PTEST_PATH}
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
