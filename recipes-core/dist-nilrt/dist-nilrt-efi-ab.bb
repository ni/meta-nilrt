SUMMARY = "IPK to install the current DISTRO_VERSION of the minimal image on an exising NXG target"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PV = "${DISTRO_VERSION}"
BUNDLE = "minimal-nilrt-bundle"
DEPENDS = "${BUNDLE}"
RDEPENDS_${PN} += "bash rauc"
ALLOW_EMPTY_${PN}-dbg = "0"
ALLOW_EMPTY_${PN}-dev = "0"

SRC_URI += "\
    file://efi-ab/${PN}-install \
"

do_install[depends] = " \
    ${BUNDLE}:do_deploy \
"

do_install_x64() {
    install -d ${D}/sbin
    install -m 0755 ${WORKDIR}/efi-ab/${PN}-install  ${D}/sbin/nilrt-install
    install -m 0755 ${DEPLOY_DIR_IMAGE}/${BUNDLE}-${MACHINE}.raucb ${D}
}

FILES_${PN} = "\
    /${BUNDLE}-${MACHINE}.raucb \
    /sbin/nilrt-install \
"
