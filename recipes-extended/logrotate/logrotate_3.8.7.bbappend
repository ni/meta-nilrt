FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://logrotate.crontab \
	    file://logrotate.conf \
"

do_install_append(){
    rm -rf ${D}${sysconfdir}/cron.daily
    install -d ${D}${sysconfdir}/cron/crontabs/
    cat ${WORKDIR}/logrotate.crontab >> ${D}${sysconfdir}/cron/crontabs/root
    install -p -m 644 ${WORKDIR}/logrotate.conf ${D}${sysconfdir}/logrotate.conf
}

