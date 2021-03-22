SUMMARY = "IPK to repartition a safemode based installation to an A/B EFI implementation"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

LICENSE_CREATE_PACKAGE = "0"

SRC_URI = "\
    file://nilrt-gateway-install \
    file://MIT \
"
FILESEXTRAPATHS_prepend := "${COMMON_LICENSE_DIR}:"

PV = "${DISTRO_VERSION}"
RDEPENDS_${PN} += "bash"
ALLOW_EMPTY_${PN}-dbg = "0"
ALLOW_EMPTY_${PN}-dev = "0"

ISO_NAME = "nilrt-restore-mode"
IMAGE = "${ISO_NAME}-image"
do_install[depends] = "${IMAGE}:do_image_complete"

do_install_append_x64() {
    install -d ${D}/usr/share/nilrt
    install -d ${D}/usr/share/licenses/${PN}
    install -m 0755 ${WORKDIR}/nilrt-gateway-install ${D}/usr/share/nilrt/nilrt-install
    install -m 0755 ${WORKDIR}/MIT ${D}/usr/share/licenses/${PN}/MIT
    install -m 0755 ${DEPLOY_DIR_IMAGE}/${ISO_NAME}-${MACHINE}.wic ${D}/usr/share/nilrt/restore-mode-image-${MACHINE}.iso
}

python do_package_ipk_prepend() {
    d.setVar('OPKGBUILDCMD', 'opkg-build')
}

python do_package_ipk_append() {
    d.setVar('OPKGBUILDCMD', '')
}

FILES_${PN}_x64 = "\
    /usr/share/nilrt/restore-mode-image-${MACHINE}.iso \
    /usr/share/nilrt/nilrt-install \
    /usr/share/licenses/${PN}/MIT \
"
