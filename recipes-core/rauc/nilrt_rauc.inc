FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
PACKAGECONFIG:remove += "network json service"

INSANE_SKIP:${PN} += "native-last"
inherit update-rc.d

RDEPENDS:${PN} += "dosfstools e2fsprogs bzip2"

SRC_URI += " \
    file://rauc-mark-good \
"

RDEPENDS:${PN}-mark-good += "${PN} bash transconf"
FILES:${PN}-mark-good = "${sysconfdir}/init.d/rauc-mark-good /var/lib/rauc/"

INITSCRIPT_PACKAGES += "${PN}-mark-good"
INITSCRIPT_NAME = ""
INITSCRIPT_NAME:${PN}-mark-good = "rauc-mark-good"
INITSCRIPT_PARAMS:${PN}-mark-good = "start 30 S ."

do_install:append () {

    install -d ${D}${sysconfdir}/init.d/
    install -m 0755 ${WORKDIR}/rauc-mark-good ${D}${sysconfdir}/init.d/

    # status directory
    install -d ${D}/var/lib/rauc/
}
