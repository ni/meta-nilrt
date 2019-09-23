FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
PACKAGECONFIG_remove += "network json service"

inherit update-rc.d

RDEPENDS_${PN} += "dosfstools e2fsprogs bzip2"

SRC_URI += " \
    file://rauc-mark-good \
    file://0001-rauc-Add-command-line-option-to-skip-bundle-verifica.patch \
"

RDEPENDS_${PN}-mark-good += "${PN} bash transconf"
FILES_${PN}-mark-good = "${sysconfdir}/init.d/rauc-mark-good /var/lib/rauc/"

INITSCRIPT_PACKAGES += "${PN}-mark-good"
INITSCRIPT_NAME = ""
INITSCRIPT_NAME_${PN}-mark-good = "rauc-mark-good"
INITSCRIPT_PARAMS_${PN}-mark-good = "start 30 S ."

do_install_append () {

    install -d ${D}${sysconfdir}/init.d/
    install -m 0755 ${WORKDIR}/rauc-mark-good ${D}${sysconfdir}/init.d/

    # status directory
    install -d ${D}/var/lib/rauc/
}
