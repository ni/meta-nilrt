FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://ipsec.init \
"

PACKAGECONFIG += " \
    aikgen \
    eap-identity \
    eap-mschapv2 \
    imc-test \
    imv-test \
    imc-scanner \
    imv-scanner \
    imc-os \
    imv-os \
    imc-attestation \
    imv-attestation \
    imc-swima \
    imv-swima \
    tnc-ifmap \
    tnc-imc \
    tnc-imv \
    tnc-pdp \
    tnccs-11 \
    tnccs-20 \
    tnccs-dynamic \
    tss-trousers \
    "

PACKAGECONFIG[aikgen] = "--enable-aikgen,--disable-aikgen,,"
PACKAGECONFIG[imc-test] = "--enable-imc-test,--disable-imc-test,,"
PACKAGECONFIG[imv-test] = "--enable-imv-test,--disable-imv-test,,"
PACKAGECONFIG[imc-scanner] = "--enable-imc-scanner,--disable-imc-scanner,,"
PACKAGECONFIG[imv-scanner] = "--enable-imv-scanner,--disable-imv-scanner,,"
PACKAGECONFIG[imc-os] = "--enable-imc-os,--disable-imc-os,,"
PACKAGECONFIG[imv-os] = "--enable-imv-os,--disable-imv-os,,"
PACKAGECONFIG[imc-attestation] = "--enable-imc-attestation,--disable-imc-attestation,,"
PACKAGECONFIG[imv-attestation] = "--enable-imv-attestation,--disable-imv-attestation,,"
PACKAGECONFIG[imc-swima] = "--enable-imc-swima,--disable-imc-swima,json-c,"
PACKAGECONFIG[imv-swima] = "--enable-imv-swima,--disable-imv-swima,json-c,"
PACKAGECONFIG[tnc-ifmap] = "--enable-tnc-ifmap,--disable-tnc-ifmap,libxml2,"
PACKAGECONFIG[tnc-imc] = "--enable-tnc-imc,--disable-tnc-imc,,"
PACKAGECONFIG[tnc-imv] = "--enable-tnc-imv,--disable-tnc-imv,,"
PACKAGECONFIG[tnc-pdp] = "--enable-tnc-pdp,--disable-tnc-pdp,,"
PACKAGECONFIG[tnccs-11] = "--enable-tnccs-11,--disable-tnccs-11,libxml2,"
PACKAGECONFIG[tnccs-20] = "--enable-tnccs-20,--disable-tnccs-20,,"
PACKAGECONFIG[tnccs-dynamic] = "--enable-tnccs-dynamic,--disable-tnccs-dynamic,,"
PACKAGECONFIG[tss-trousers] = "--enable-tss-trousers,,libtspi,"

FILES_${PN} += "${libdir}/ipsec/imcvs/*.so ${datadir}/regid.2004-03.org.strongswan"
FILES_${PN}-dbg += "${libdir}/ipsec/imcvs/.debug"
FILES_${PN}-dev += "${libdir}/ipsec/imcvs/*.la"
FILES_${PN}-staticdev += "${libdir}/ipsec/imcvs/*.a"

inherit update-rc.d

INITSCRIPT_NAME = "ipsec"
INITSCRIPT_PARAMS = "defaults 42"

do_install_append() {
    if ${@bb.utils.contains('DISTRO_FEATURES', 'sysvinit', 'true', 'false', d)}; then
        install -d ${D}${sysconfdir}/init.d
        install -m 0755 ${WORKDIR}/ipsec.init ${D}${sysconfdir}/init.d/ipsec
    fi
}
