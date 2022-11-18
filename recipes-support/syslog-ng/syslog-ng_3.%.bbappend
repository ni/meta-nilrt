FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://nikern.conf \
            file://logrotate.d-auth.conf \
            file://logrotate.d-cron.conf \
            file://logrotate.d-messages.conf \
"

RRECOMMENDS:${PN}:append := "logrotate"

DEPENDS += "shadow-native pseudo-native niacctbase"

RDEPENDS:${PN} += "niacctbase"

CONFFILES:${PN}:append := " ${sysconfdir}/logrotate.d/auth.conf \
                            ${sysconfdir}/logrotate.d/cron.conf \
                            ${sysconfdir}/logrotate.d/messages.conf"

do_install:append (){
   install -d ${D}${sysconfdir}/syslog-ng.d
   install -d ${D}${sysconfdir}/logrotate.d

   install -m 644 ${WORKDIR}/nikern.conf ${D}${sysconfdir}/logrotate.d/

   install -d -m 0755 ${D}${localstatedir}/local/natinst/log/
   chown ${LVRT_USER}:${LVRT_GROUP} ${D}${localstatedir}/local/natinst/log/

   install -m 0644 ${WORKDIR}/logrotate.d-auth.conf ${D}${sysconfdir}/logrotate.d/auth.conf
   install -m 0644 ${WORKDIR}/logrotate.d-cron.conf ${D}${sysconfdir}/logrotate.d/cron.conf
   install -m 0644 ${WORKDIR}/logrotate.d-messages.conf ${D}${sysconfdir}/logrotate.d/messages.conf

   # NILRT has a custom conf file for sysvinit systems.
   # If using sysvinit, update the conf file with the current version.
   if ${@bb.utils.contains('DISTRO_FEATURES','systemd','false','true',d)}; then
      # syslog-ng expects the version to be MAJ.MIN, so filter out the patch version from ${PV}.
      sed -i -e "s/@VERSION@/$(echo ${PV} | grep -oE ^[0-9]+.[0-9]+)/g" ${D}${sysconfdir}/${BPN}/${BPN}.conf
   fi
}

INITSCRIPT_PARAMS = "start 19 2 3 4 5 . stop 90 0 1 6 ."
