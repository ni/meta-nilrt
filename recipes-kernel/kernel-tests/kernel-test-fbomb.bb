SUMMARY = "Linux kernel futex test"
HOMEPAGE = "https://kernel.org"
SECTION = "tests"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://fbomb.c;md5=5087cbd611aca643601e03428b6ef30d"
inherit ptest

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}-files:"

S = "${WORKDIR}"

DEPENDS = "virtual/kernel"
RDEPENDS:${PN}-ptest += "bash kmod"

ALLOW_EMPTY:${PN} = "1"

SRC_URI += "\
    file://run-ptest \
    file://fbomb.c \
"

LDFLAGS += "-lpthread"

do_compile_ptest:append() {
    cd ${WORKDIR}
    ${CC} ${CFLAGS} -o fbomb fbomb.c ${LDFLAGS}
}

do_install_ptest:append() {
    install -m 0755 ${S}/run-ptest ${D}${PTEST_PATH}
    install -m 0755 ${S}/fbomb ${D}${PTEST_PATH}
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
