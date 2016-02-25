FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

RRECOMMENDS_${PN}_append := "logrotate"

SRC_URI_append = "  file://logrotate.d-auth.conf \
                    file://logrotate.d-cron.conf \
                    file://logrotate.d-messages.conf"

CONFFILES_${PN}_append := " ${sysconfdir}/logrotate.d/auth.conf \
                            ${sysconfdir}/logrotate.d/cron.conf \
                            ${sysconfdir}/logrotate.d/messages.conf"
do_install_append(){
    install -d ${D}${sysconfdir}/syslog-ng.d
    install -d ${D}${sysconfdir}/logrotate.d
    install -m 0644 ${WORKDIR}/logrotate.d-auth.conf ${D}${sysconfdir}/logrotate.d/auth.conf
    install -m 0644 ${WORKDIR}/logrotate.d-cron.conf ${D}${sysconfdir}/logrotate.d/cron.conf
    install -m 0644 ${WORKDIR}/logrotate.d-messages.conf ${D}${sysconfdir}/logrotate.d/messages.conf
}
