FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://05-penmount.conf"

do_install_append() {
    install -m 0644 ${WORKDIR}/05-penmount.conf ${D}${datadir}/X11/xorg.conf.d/05-penmount.conf
}
