FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://icons/32x32/xfce4-panel.png \
	   file://icons/32x32/xfce4-panel-menu.png \
	   file://icons/48x48/xfce4-panel.png \
	   file://icons/48x48/xfce4-panel-menu.png \
	   file://icons/16x16/xfce4-panel.png \
	   file://icons/16x16/xfce4-panel-menu.png \
	   file://icons/22x22/xfce4-panel.png \
	   file://icons/22x22/xfce4-panel-menu.png \
	   file://icons/24x24/xfce4-panel.png \
	   file://icons/24x24/xfce4-panel-menu.png"

do_unpack_append() {
    os.system('cp -af "${WORKDIR}/icons" "${S}/"')
    os.system('rm -r "${WORKDIR}/icons"')
}
