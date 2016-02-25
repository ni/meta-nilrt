FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += " \
		file://logrotate.crontab \
		file://logrotate.conf \
		file://0001-Support-system-dates-back-to-the-year-1970.patch \
"

do_install_append(){
    rm -rf ${D}${sysconfdir}/cron.daily/logrotate
    install -d ${D}${sysconfdir}/cron.d
    cat ${WORKDIR}/logrotate.crontab >> ${D}${sysconfdir}/cron.d/logrotate
    install -m 644 ${WORKDIR}/logrotate.conf ${D}${sysconfdir}
}

