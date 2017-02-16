FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://icons.tar.xz;unpack=false"

do_install_append() {
    tar -xf ${WORKDIR}/icons.tar.xz -C ${D}
}
