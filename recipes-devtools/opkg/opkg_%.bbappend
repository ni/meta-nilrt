FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

inherit ptest

SRC_URI += " \
            file://opkg.conf \
            file://opkg-signing.conf \
            file://run-ptest \
            file://0001-libsolv_solver_init-make-no-install-recommends-case-.patch \
            file://0002-libsolv_solver_execute_transaction-propagate-downloa.patch \
"

SRC_URI_append_armv7a = " \
            file://arm-kernel-arch.conf \
            file://test_arm_kernel_arch.sh \
"

PACKAGECONFIG = "libsolv gpg sha256"

RDEPENDS_${PN}-ptest += "bash"

do_install_append () {
    install -d ${D}${sysconfdir}/opkg
    install -m 0644 ${WORKDIR}/opkg-signing.conf ${D}${sysconfdir}/opkg/
}

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
