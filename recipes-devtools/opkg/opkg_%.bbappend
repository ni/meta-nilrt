FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

inherit ptest

SRC_URI += " \
            file://0001-libopkg-Add-intercepts_dir-option-to-opkg.conf.patch \
            file://test_feedserver.sh \
            file://test_ni_pxi_install.sh \
            file://run-ptest \
           "

RDEPENDS_${PN}-ptest += "bash"

do_install_ptest() {
        cp ${WORKDIR}/test_feedserver.sh ${D}${PTEST_PATH}
        cp ${WORKDIR}/test_ni_pxi_install.sh ${D}${PTEST_PATH}
}
