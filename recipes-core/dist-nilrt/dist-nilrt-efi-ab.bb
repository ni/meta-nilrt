SUMMARY = "IPK to install the current DISTRO_VERSION of the minimal image on an exising NXG target"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

LICENSE_CREATE_PACKAGE = "0"

PV = "${DISTRO_VERSION}"
BUNDLE = "minimal-nilrt-bundle"
DEPENDS = "${BUNDLE}"
RDEPENDS:${PN} += "bash"
ALLOW_EMPTY:${PN}-dbg = "0"
ALLOW_EMPTY:${PN}-dev = "0"

SRC_URI += "\
    file://${PN}-install \
    file://MIT \
"

FILESEXTRAPATHS:prepend := "${COMMON_LICENSE_DIR}:"

do_install[depends] = " \
    ${BUNDLE}:do_deploy \
"

do_install:x64() {
    install -d ${D}/usr/share/nilrt
    install -d ${D}/usr/share/licenses/${PN}
    install -m 0755 ${WORKDIR}/${PN}-install  ${D}/usr/share/nilrt/nilrt-install
    install -m 0755 ${WORKDIR}/MIT ${D}/usr/share/licenses/${PN}/MIT
}

FILES:${PN} = "\
    /usr/share/nilrt/nilrt-install \
    /usr/share/licenses/${PN}/MIT \
"
