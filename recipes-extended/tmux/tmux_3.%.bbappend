FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

CONFFILES += "\
    file://tmux.conf \
"

FILES:${PN}     += "${sysconfdir}/tmux.conf"
CONFFILES:${PN} += "${sysconfdir}/tmux.conf"

do_install:append() {
    install -d ${D}${sysconfdir}
    install -m 644 ${WORKDIR}/tmux.conf ${D}${sysconfdir}/tmux.conf
}

RDEPENDS:${PN}:append = " vlock"
