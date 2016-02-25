FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

do_install_append(){
    install -d ${D}${sysconfdir}/syslog-ng.d
}
