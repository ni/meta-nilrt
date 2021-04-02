SUMMARY = "IPK to install the current DISTRO_VERSION of base bundle"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

LICENSE_CREATE_PACKAGE = "0"

PV = "${DISTRO_VERSION}"
BUNDLE = "nilrt-base-bundle"
DEPENDS = "${BUNDLE}"
RDEPENDS_${PN} += "bash rauc"
ALLOW_EMPTY_${PN}-dbg = "0"
ALLOW_EMPTY_${PN}-dev = "0"

SRC_URI += "\
    file://${PN}-install \
    file://MIT \
"

FILESEXTRAPATHS_prepend := "${COMMON_LICENSE_DIR}:"

do_install[depends] = " \
    ${BUNDLE}:do_deploy \
"

do_install_x64() {
    install -d ${D}/usr/share/nilrt
    install -d ${D}/usr/share/licenses/${PN}
    install -m 0755 ${WORKDIR}/${PN}-install  ${D}/usr/share/nilrt/nilrt-install
    install -m 0755 ${WORKDIR}/MIT ${D}/usr/share/licenses/${PN}/MIT
    install -m 0755 ${DEPLOY_DIR_IMAGE}/${BUNDLE}-${MACHINE}.raucb ${D}/usr/share/nilrt
}

FILES_${PN} = "\
    /usr/share/nilrt/${BUNDLE}-${MACHINE}.raucb \
    /usr/share/nilrt/nilrt-install \
    /usr/share/licenses/${PN}/MIT \
"
