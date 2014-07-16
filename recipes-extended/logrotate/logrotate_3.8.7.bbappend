FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PRINC := "${@int(PRINC) + 1}"

SRC_URI += "file://logrotate.crontab \
"

do_install_append(){
    rm -rf ${D}${sysconfdir}/cron.daily
    install -d ${D}${sysconfdir}/cron/crontabs/
    cat ${WORKDIR}/logrotate.crontab >> ${D}${sysconfdir}/cron/crontabs/root
}

