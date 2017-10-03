FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

inherit ptest

SRC_URI += " \
            file://opkg.conf \
            file://test_feedserver.sh \
            file://run-ptest \
            file://0001-set_flags_from_control-remove-function.patch \
            file://0002-pkg_formatted_field-error-out-in-all-unknown-fields.patch \
            file://0003-libopkg-add-option-verbose_status_file.patch \
            file://0004-libopkg-add-support-for-user-defined-fields.patch \
            file://0005-parse_userfields-parse-values-until-newline.patch \
            file://0006-buffer-overrun-fix.patch \
           "

SRC_URI_append_x64 = " \
            file://test_ni_pxi_install.sh \
"

SRC_URI_append_armv7a = " \
            file://arm-kernel-arch.conf \
"

PACKAGECONFIG = "libsolv"

RDEPENDS_${PN}-ptest += "bash"

do_install_ptest() {
    install -d ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_feedserver.sh ${D}${PTEST_PATH}
}

do_install_ptest_append_x64() {
    install -m 755 ${WORKDIR}/test_ni_pxi_install.sh ${D}${PTEST_PATH}
}

do_install_append_armv7a () {
    install -d ${D}${sysconfdir}/opkg
    install -m 0644 ${WORKDIR}/arm-kernel-arch.conf ${D}${sysconfdir}/opkg/
}
