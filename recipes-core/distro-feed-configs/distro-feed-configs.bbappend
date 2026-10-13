FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

inherit ptest

SRC_URI:append = " \
    file://run-ptest \
    file://ptest-format.sh \
    file://test_feed_listings.sh \
"

RDEPENDS:${PN}-ptest += " bash "

do_install_ptest() {
    install -d ${D}${PTEST_PATH}
    install -m 755 ${UNPACKDIR}/test_feed_listings.sh ${D}${PTEST_PATH}
    install -m 644 ${UNPACKDIR}/ptest-format.sh ${D}${PTEST_PATH}
    install -m 755 ${UNPACKDIR}/run-ptest ${D}${PTEST_PATH}
}
