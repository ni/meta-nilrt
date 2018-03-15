FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
SRC_URI_append := "file://wired-iface.conf"
do_install_append () {

    # Append extra conf file
    install -m 0755 -d ${D}${sysconfdir}/lldpd.d
    install -m 0644 ${WORKDIR}/wired-iface.conf ${D}${sysconfdir}/lldpd.d/wired-iface.conf
}
