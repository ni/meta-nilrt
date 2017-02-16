FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://xinput_calibrator_wrapper \
            file://add-xfce-settings-and-wrapper-to-desktop-file.patch"

do_install_append() {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/xinput_calibrator_wrapper ${D}${bindir}/xinput_calibrator_wrapper
}
