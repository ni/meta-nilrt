FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://scripts/xinput_calibrator_wrapper \
	   file://scripts/xinput_calibrator.desktop"

do_unpack_append() {
    os.system('cp -af "${WORKDIR}/scripts" "${S}/"')
    os.system('rm -r "${WORKDIR}/scripts"')
}

do_install_append() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/scripts/xinput_calibrator_wrapper ${D}${bindir}/xinput_calibrator_wrapper
}
