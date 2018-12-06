FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += " \
    file://nilrt-feed-2019.gpg \
"

do_install_append() {

    # Install NI signing keys
    install -m 0444 ${WORKDIR}/nilrt-feed-2019.gpg ${D}${datadir}/opkg/keyrings/
}
