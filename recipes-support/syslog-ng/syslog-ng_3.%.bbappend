FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "\
	file://logrotate.d-auth.conf \
	file://logrotate.d-cron.conf \
	file://logrotate.d-messages.conf \
	file://nikern.conf \
"

DEPENDS += "shadow-native pseudo-native niacctbase"

INITSCRIPT_PARAMS = "start 19 2 3 4 5 . stop 90 0 1 6 ."


do_install:append (){
	install -d ${D}${sysconfdir}/syslog-ng.d
	install -d ${D}${sysconfdir}/logrotate.d

	install -m 644 ${WORKDIR}/nikern.conf ${D}${sysconfdir}/logrotate.d/

	install -d -m 0755 ${D}${localstatedir}/local/natinst/log/
	chown ${LVRT_USER}:${LVRT_GROUP} ${D}${localstatedir}/local/natinst/log/

	install -m 0644 ${WORKDIR}/logrotate.d-auth.conf ${D}${sysconfdir}/logrotate.d/auth.conf
	install -m 0644 ${WORKDIR}/logrotate.d-cron.conf ${D}${sysconfdir}/logrotate.d/cron.conf
	install -m 0644 ${WORKDIR}/logrotate.d-messages.conf ${D}${sysconfdir}/logrotate.d/messages.conf
}

CONFFILES:${PN}:append := "\
	${sysconfdir}/logrotate.d/auth.conf \
	${sysconfdir}/logrotate.d/cron.conf \
	${sysconfdir}/logrotate.d/messages.conf \
"

RDEPENDS:${PN} += "niacctbase"
RRECOMMENDS:${PN}:append := "logrotate"
