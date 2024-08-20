FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append = "\
    file://pwquality.conf \
"

do_install:append() {
    install -d ${D}${sysconfdir}/security
    install -m 644 ${WORKDIR}/pwquality.conf ${D}${sysconfdir}/security/pwquality.conf
}

FILES:${PN}     += "${sysconfdir}/security/pwquality.conf"
CONFFILES:${PN} += "${sysconfdir}/security/pwquality.conf"
