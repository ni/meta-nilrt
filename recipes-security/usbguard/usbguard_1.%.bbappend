FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://usbguard.init \
"

inherit update-rc.d

INITSCRIPT_NAME = "usbguard"
INITSCRIPT_PARAMS = "start 20 2 3 4 5 . stop 80 0 1 6 ."

# Runtime dependencies for proper operation
RDEPENDS:${PN} += "bash"

do_install:append() {
    if ${@bb.utils.contains('DISTRO_FEATURES', 'sysvinit', 'true', 'false', d)}; then
        install -d ${D}${sysconfdir}/init.d
        install -m 0755 ${WORKDIR}/usbguard.init ${D}${sysconfdir}/init.d/usbguard
        
        # Remove /etc/volatile.cache if it exists in the target image
        rm -f ${D}${sysconfdir}/volatile.cache
    fi
}
