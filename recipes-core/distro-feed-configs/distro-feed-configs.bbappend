FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

inherit ptest

SRC_URI_append = " \
    file://run-ptest \
    file://ptest-format.sh \
    file://test_feed_listings.sh \
"

RDEPENDS_${PN}-ptest += " bash "

do_install_ptest() {
    install -d ${D}${PTEST_PATH}
    install -m 755 ${WORKDIR}/test_feed_listings.sh ${D}${PTEST_PATH}
    install -m 644 ${WORKDIR}/ptest-format.sh ${D}${PTEST_PATH}
    install -m 755 ${WORKDIR}/run-ptest ${D}${PTEST_PATH}
}
