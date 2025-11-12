FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://xinput_calibrator_wrapper\
            file://0001-add-xfce-settings-and-wrapper-to-.desktop-file.patch \
            "

do_install:append() {
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/xinput_calibrator_wrapper ${D}${bindir}/xinput_calibrator_wrapper
}
