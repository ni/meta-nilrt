FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://xinput_calibrator_wrapper \
	   file://xinput_calibrator.desktop"

do_install_append() {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/xinput_calibrator_wrapper ${D}${bindir}/xinput_calibrator_wrapper
}
