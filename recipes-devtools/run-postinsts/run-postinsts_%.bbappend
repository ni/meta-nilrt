FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

inherit ptest

SRC_URI_append = " \
    file://postinst.default.sh \
    file://run-ptest \
    file://test-opkg-status.sh \
    file://test-run-postinsts-log.sh \
"

do_install_append() {

    # Install a run-postinsts config file
    install -d ${D}${sysconfdir}/
    install -d ${D}${sysconfdir}/default/

    install -m 0755 ${WORKDIR}/postinst.default.sh ${D}${sysconfdir}/default/postinst
}

do_install_ptest_append() {

    # Install test cases
    install -m 0755 ${WORKDIR}/test-opkg-status.sh ${D}${PTEST_PATH}/
    install -m 0755 ${WORKDIR}/test-run-postinsts-log.sh ${D}${PTEST_PATH}/
}
