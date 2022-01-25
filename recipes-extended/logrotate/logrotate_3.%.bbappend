FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += " \
		file://logrotate.crontab \
		file://logrotate.conf \
		file://btmp \
		file://wtmp \
"

do_install_append(){
    rm -rf ${D}${sysconfdir}/cron.daily/logrotate
    install -d ${D}${sysconfdir}/cron.d
    cat ${WORKDIR}/logrotate.crontab >> ${D}${sysconfdir}/cron.d/logrotate
    # logrotate.conf, btmp, and wtmp config files are owned by logrotate
    # upstream installs example versions of these config files
    # we are intentionally overwriting them here with our own versions
    install -p -m 644 ${WORKDIR}/logrotate.conf ${D}${sysconfdir}/logrotate.conf
    install -p -m 644 ${WORKDIR}/btmp ${D}${sysconfdir}/logrotate.d/btmp
    install -p -m 644 ${WORKDIR}/wtmp ${D}${sysconfdir}/logrotate.d/wtmp
}

