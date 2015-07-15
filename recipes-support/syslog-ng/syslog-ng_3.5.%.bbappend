FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += "file://nikern.conf \
"

do_install_append (){
   install -d ${D}${sysconfdir}/logrotate.d
   install -m 644 ${WORKDIR}/nikern.conf ${D}${sysconfdir}/logrotate.d/
}
