FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://logrotate.crontab \
"

do_install_append(){
    rm -rf ${D}${sysconfdir}/cron.daily/logrotate
    install -d ${D}${sysconfdir}/cron.d
    cat ${WORKDIR}/logrotate.crontab >> ${D}${sysconfdir}/cron.d/logrotate
}

