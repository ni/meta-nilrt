FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

inherit ptest

SRC_URI += " \
            file://opkg.conf \
            file://opkg-signing.conf \
            file://gpg.conf \
            file://run-ptest \
            file://0001-opkg-key-add-keys-even-if-creation-date-is-in-the-fu.patch \
"

SRC_URI_append_armv7a = " \
            file://arm-kernel-arch.conf \
            file://test_arm_kernel_arch.sh \
"

PACKAGECONFIG = "libsolv gpg sha256 curl"

RDEPENDS_${PN}-ptest += "bash"

do_install_append () {
    install -d ${D}${sysconfdir}/opkg
    install -m 0644 ${WORKDIR}/opkg-signing.conf ${D}${sysconfdir}/opkg/
    install -d -m 0700 ${D}${sysconfdir}/opkg/gpg
    install -m 0644 ${WORKDIR}/gpg.conf ${D}${sysconfdir}/opkg/gpg/
}

do_install_append_armv7a () {
    install -d ${D}${sysconfdir}/opkg
    install -m 0644 ${WORKDIR}/arm-kernel-arch.conf ${D}${sysconfdir}/opkg/
}

do_install_ptest_append_armv7a () {
    install -m 0755 ${WORKDIR}/test_arm_kernel_arch.sh ${D}${PTEST_PATH}
}
