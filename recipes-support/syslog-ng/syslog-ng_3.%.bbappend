FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += "file://nikern.conf \
"

RDEPENDS_${PN} += "niacctbase"

do_install_append (){
   install -d ${D}${sysconfdir}/logrotate.d
   install -m 644 ${WORKDIR}/nikern.conf ${D}${sysconfdir}/logrotate.d/
   install -d -m 0755 ${D}${localstatedir}/local/natinst/log/
   chown ${LVRT_USER}:${LVRT_GROUP} ${D}${localstatedir}/local/natinst/log/
}

INITSCRIPT_PARAMS = "start 19 2 3 4 5 . stop 90 0 1 6 ."
