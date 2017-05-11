FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

inherit ptest

SRC_URI += " \
            file://opkg.conf \
            file://test_feedserver.sh \
            file://test_ni_pxi_install.sh \
            file://run-ptest \
            file://0001-set_flags_from_control-remove-function.patch \
            file://0002-pkg_formatted_field-error-out-in-all-unknown-fields.patch \
            file://0003-libopkg-add-option-verbose_status_file.patch \
            file://0004-libopkg-add-support-for-user-defined-fields.patch \
           "

PACKAGECONFIG = "libsolv"

RDEPENDS_${PN}-ptest += "bash"

do_install_ptest() {
        cp ${WORKDIR}/test_feedserver.sh ${D}${PTEST_PATH}
        cp ${WORKDIR}/test_ni_pxi_install.sh ${D}${PTEST_PATH}
}
