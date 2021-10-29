LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PACKAGES_remove = "${PN}-dev ${PN}-staticdev ${PN}-dbg"

SRC_URI_append = "\
    file://ptest-format.sh \
    file://run-ptest \
    file://test_hwclock.sh \
"

inherit ptest

do_install_ptest() {
    install -m 0644 ${WORKDIR}/ptest-format.sh ${D}${PTEST_PATH}
    install -m 0755 ${WORKDIR}/run-ptest       ${D}${PTEST_PATH}
    install -m 0755 ${WORKDIR}/test_hwclock.sh ${D}${PTEST_PATH}
}

RDEPENDS_${PN}-ptest += " bash "
