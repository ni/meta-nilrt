SUMMARY = "Linux kernel futex test"
HOMEPAGE = "https://kernel.org"
SECTION = "tests"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://fbomb.c;md5=5087cbd611aca643601e03428b6ef30d"


DEPENDS = "virtual/kernel"


S = "${WORKDIR}"

SRC_URI += "\
	file://run-ptest \
	file://fbomb.c \
"


inherit ptest
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}-files:"
LDFLAGS += "-lpthread"
ALLOW_EMPTY:${PN} = "1"


do_compile_ptest:append() {
	cd ${WORKDIR}
	${CC} ${CFLAGS} -o fbomb fbomb.c ${LDFLAGS}
}

do_install_ptest:append() {
	install -m 0755 ${S}/run-ptest ${D}${PTEST_PATH}
	install -m 0755 ${S}/fbomb ${D}${PTEST_PATH}
}


PACKAGE_ARCH = "${MACHINE_ARCH}"
RDEPENDS:${PN}-ptest += "bash kmod"
