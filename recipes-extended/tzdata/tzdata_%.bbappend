INSTALL_TIMEZONE_FILE = "0"

DEFAULT_TIMEZONE = "UTC"

do_install_append () {
    # Install /etc/natinst/share/localtime
    install -d ${D}${sysconfdir}/natinst/share/
    ln -s ${datadir}/zoneinfo/${DEFAULT_TIMEZONE} ${D}${sysconfdir}/natinst/share/localtime

    # Redirect /etc/localtime --> /etc/natinst/share/localtime
    rm ${D}${sysconfdir}/localtime
    ln -s ${sysconfdir}/natinst/share/localtime ${D}${sysconfdir}/localtime
}
