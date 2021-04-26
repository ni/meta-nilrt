FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://ipsec.init \
"

inherit update-rc.d

INITSCRIPT_NAME = "ipsec"
INITSCRIPT_PARAMS = "defaults 42"

do_install_append() {
    if ${@bb.utils.contains('DISTRO_FEATURES', 'sysvinit', 'true', 'false', d)}; then
        install -d ${D}${sysconfdir}/init.d
        install -m 0755 ${WORKDIR}/ipsec.init ${D}${sysconfdir}/init.d/ipsec
    fi
}
