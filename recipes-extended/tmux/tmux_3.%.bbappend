FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "\
    file://tmux.conf \
"

do_install:append() {
    install -d ${D}${sysconfdir}
    install -m 644 ${WORKDIR}/tmux.conf ${D}${sysconfdir}/tmux.conf
    install -d ${D}/usr/share/tmux/conf.d
}

FILES:${PN}     += "${sysconfdir}/tmux.conf"
CONFFILES:${PN} += "${sysconfdir}/tmux.conf"
RDEPENDS:${PN}:append = " vlock"
