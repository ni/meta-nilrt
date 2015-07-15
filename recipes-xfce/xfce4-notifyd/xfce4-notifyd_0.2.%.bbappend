FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://icons/48x48/xfce4-notifyd.png"

do_unpack_append() {
    os.system('cp -af "${WORKDIR}/icons" "${S}/"')
    os.system('rm -r "${WORKDIR}/icons"')
}