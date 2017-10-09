SUMMARY = "Disk crypto utilities for NILRT"
DESCRIPTION = "Utilities for manging encrypted storage on NILRT systems"
SECTION = "base"

LICENSE = "MIT"
LIC_FILES_CHKSUM = " \
    file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420 \
"

inherit allarch ptest

DEPENDS += "bash busybox coreutils tpm2-tools openssl cryptsetup"

PACKAGES =+ "${PN}-common ${PN}-open ${PN}-reseal"

RDEPENDS_${PN}-common += "bash busybox coreutils-shred coreutils-timeout tpm2-tools cryptsetup"
RDEPENDS_${PN}-open += "${PN}-common"
RDEPENDS_${PN}-reseal += "${PN}-common"
RDEPENDS_${PN} += "${PN}-common ${PN}-open ${PN}-reseal openssl"

FILES_${PN}-common = "${libdir}/nilrtdiskcrypt.common"
FILES_${PN}-open = "${sbindir}/nilrtdiskcrypt_open ${sbindir}/nilrtdiskcrypt_canopen"
FILES_${PN}-reseal = "${sbindir}/nilrtdiskcrypt_reseal"

RDEPENDS_${PN}-ptest += "${PN}"
FILES_${PN}-ptest += "${PTEST_PATH}"

S = "${WORKDIR}"

SRC_URI = " \
    file://nilrtdiskcrypt.common \
    file://nilrtdiskcrypt_open \
    file://nilrtdiskcrypt_canopen \
    file://nilrtdiskcrypt_reseal \
    file://nilrtdiskcrypt_unseal \
    file://nilrtdiskcrypt_disableunseal \
    file://nilrtdiskcrypt_close \
    file://nilrtdiskcrypt_format \
    file://nilrtdiskcrypt_canformat \
    file://nilrtdiskcrypt_wipe \
    file://nilrtdiskcrypt_test_tpm \
"

do_install () {
    install -d ${D}${libdir}
    install -d ${D}${sbindir}

    install -m 0644 ${S}/nilrtdiskcrypt.common ${D}${libdir}/

    install -m 0755 ${S}/nilrtdiskcrypt_open ${D}${sbindir}/
    install -m 0755 ${S}/nilrtdiskcrypt_canopen ${D}${sbindir}/
    install -m 0755 ${S}/nilrtdiskcrypt_reseal ${D}${sbindir}/
    install -m 0755 ${S}/nilrtdiskcrypt_unseal ${D}${sbindir}/
    install -m 0755 ${S}/nilrtdiskcrypt_disableunseal ${D}${sbindir}/
    install -m 0755 ${S}/nilrtdiskcrypt_close ${D}${sbindir}/
    install -m 0755 ${S}/nilrtdiskcrypt_format ${D}${sbindir}/
    install -m 0755 ${S}/nilrtdiskcrypt_canformat ${D}${sbindir}/
    install -m 0755 ${S}/nilrtdiskcrypt_wipe ${D}${sbindir}/

    # this logic is only for older nilrt and nilrt-xfce
    if ${@base_conditional('DISTRO', 'nilrt-nxg', 'false', 'true', d)}; then
        ln -sf ${sysconfdir}/natinst/share/tpm ${D}${sysconfdir}/tpm
    fi
}

do_install_ptest_append () {
    install -d ${D}${PTEST_PATH}

    install -m 0755 ${S}/nilrtdiskcrypt_test_tpm ${D}${PTEST_PATH}/
}
