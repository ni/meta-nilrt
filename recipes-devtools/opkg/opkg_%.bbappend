FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

inherit ptest

SRC_URI += " \
            file://opkg.conf \
            file://run-ptest \
            file://0001-set_flags_from_control-remove-function.patch \
            file://0002-pkg_formatted_field-error-out-in-all-unknown-fields.patch \
            file://0003-libopkg-add-option-verbose_status_file.patch \
            file://0004-libopkg-add-support-for-user-defined-fields.patch \
            file://0005-parse_userfields-parse-values-until-newline.patch \
            file://0006-buffer-overrun-fix.patch \
            file://0007-libsolv_solver_transaction_preamble-add-arch-informa.patch \
            file://0008-populate_installed_repo-add-Essential-field-support.patch \
            file://0009-libsolv_solver_execute_transaction-abort-transaction.patch \
"

SRC_URI_append_armv7a = " \
            file://arm-kernel-arch.conf \
            file://test_arm_kernel_arch.sh \
"

PACKAGECONFIG = "libsolv"

RDEPENDS_${PN}-ptest += "bash"

do_install_ptest() {
    install -d ${D}${PTEST_PATH}
}

do_install_append_armv7a () {
    install -d ${D}${sysconfdir}/opkg
    install -m 0644 ${WORKDIR}/arm-kernel-arch.conf ${D}${sysconfdir}/opkg/
}

do_install_ptest_append_armv7a () {
    install -m 0755 ${WORKDIR}/test_arm_kernel_arch.sh ${D}${PTEST_PATH}
}
