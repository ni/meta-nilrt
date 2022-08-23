# Add a ptest to makedumpfile to check that it works on the currently-
# running kernel. Unfortunately, while the range of supported versions
# is a static constant within makedumpfile, it's not exposed by any
# options to makedumpfile itself, so we have to provide our own helper
# that uses the parent recipe's makedumpfile.h.
SRC_URI:append = "\
	file://run-ptest \
	file://makedumpfile-is-kernel-supported.c \
"


inherit ptest

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"


do_compile_ptest:append() {
	${CC} ${CFLAGS} -I${S} -o ${WORKDIR}/makedumpfile-is-kernel-supported ${WORKDIR}/makedumpfile-is-kernel-supported.c ${LDFLAGS}
}

do_install_ptest:append() {
	install -m 0755 ${WORKDIR}/run-ptest ${D}${PTEST_PATH}
	install -m 0755 ${WORKDIR}/makedumpfile-is-kernel-supported ${D}${PTEST_PATH}
}


RDEPENDS_${PN}-ptest += " bash "
