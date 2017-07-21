FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += "file://nikern.conf \
            file://logrotate.d-auth.conf \
            file://logrotate.d-cron.conf \
            file://logrotate.d-messages.conf \
"

RRECOMMENDS_${PN}_append := "logrotate"

DEPENDS += "shadow-native pseudo-native niacctbase"

RDEPENDS_${PN} += "niacctbase"

CONFFILES_${PN}_append := " ${sysconfdir}/logrotate.d/auth.conf \
                            ${sysconfdir}/logrotate.d/cron.conf \
                            ${sysconfdir}/logrotate.d/messages.conf"

do_install_append (){
   install -d ${D}${sysconfdir}/syslog-ng.d
   install -d ${D}${sysconfdir}/logrotate.d

   install -m 644 ${WORKDIR}/nikern.conf ${D}${sysconfdir}/logrotate.d/

   install -d -m 0755 ${D}${localstatedir}/local/natinst/log/
   chown ${LVRT_USER}:${LVRT_GROUP} ${D}${localstatedir}/local/natinst/log/

   install -m 0644 ${WORKDIR}/logrotate.d-auth.conf ${D}${sysconfdir}/logrotate.d/auth.conf
   install -m 0644 ${WORKDIR}/logrotate.d-cron.conf ${D}${sysconfdir}/logrotate.d/cron.conf
   install -m 0644 ${WORKDIR}/logrotate.d-messages.conf ${D}${sysconfdir}/logrotate.d/messages.conf
}

INITSCRIPT_PARAMS = "start 19 2 3 4 5 . stop 90 0 1 6 ."
