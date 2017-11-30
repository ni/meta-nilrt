FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI_append = " \
    file://postinst.default.sh \
"

do_install_append() {

    # Install a run-postinsts config file
    install -d ${D}${sysconfdir}/
    install -d ${D}${sysconfdir}/default/

    install -m 0755 ${WORKDIR}/postinst.default.sh ${D}${sysconfdir}/default/postinst
}
