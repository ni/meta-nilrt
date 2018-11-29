FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

inherit ptest

SRC_URI += " \
            file://opkg.conf \
            file://run-ptest \
            file://0001-libsolv_solver_transaction_preamble-add-arch-informa.patch \
            file://0002-populate_installed_repo-add-Essential-field-support.patch \
            file://0003-libsolv_solver_execute_transaction-abort-transaction.patch \
            file://0004-libopkg_add_fields_command_line_argument.patch \
            file://0005-libopkg_add_short_description_command_line_argument.patch \
"

SRC_URI_append_armv7a = " \
            file://arm-kernel-arch.conf \
            file://test_arm_kernel_arch.sh \
"

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
