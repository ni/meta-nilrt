FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI_append = "\
    file://ptest-format.sh \
    file://run-ptest \
    file://test_hwclock.sh \
"

inherit ptest

# Do not perform update-rc.d actions on the hwclock.sh initscript in this
# package. We only wish to call hwclock.sh from /etc/init.d/bootmisc manually.
INITSCRIPT_PACKAGES_remove = "${PN}"

do_install_ptest() {
    install -m 0644 ${WORKDIR}/ptest-format.sh ${D}${PTEST_PATH}
    install -m 0755 ${WORKDIR}/run-ptest       ${D}${PTEST_PATH}
    install -m 0755 ${WORKDIR}/test_hwclock.sh ${D}${PTEST_PATH}
}

RDEPENDS_${PN}-ptest += " bash "
