FILESEXTRAPATHS_append := "${THISDIR}/files:"

SRC_URI += "file://shadow_transconf_hook"

PACKAGES =+ "${PN}-transconf"
RDEPENDS_${PN}-transconf += "${PN} bash transconf"
FILES_${PN}-transconf = "${sysconfdir}/transconf/hooks/shadow"

do_install_append () {

    install -d ${D}${sysconfdir}
    install -d ${D}${sysconfdir}/transconf
    install -d ${D}${sysconfdir}/transconf/hooks
    install -m 0755 ${WORKDIR}/shadow_transconf_hook ${D}${sysconfdir}/transconf/hooks/shadow
}
