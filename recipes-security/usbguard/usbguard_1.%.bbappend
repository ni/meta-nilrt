FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://usbguard.init \
    file://IPCAccessControl.d/ \
"

inherit update-rc.d

INITSCRIPT_NAME = "usbguard"
INITSCRIPT_PARAMS = "stop 80 0 1 6 ."

# Runtime dependencies for proper operation
RDEPENDS:${PN} += "bash"

do_install:append() {
    if ${@bb.utils.contains('DISTRO_FEATURES', 'sysvinit', 'true', 'false', d)}; then
        install -d ${D}${sysconfdir}/init.d
        install -m 0755 ${UNPACKDIR}/usbguard.init ${D}${sysconfdir}/init.d/usbguard
        
        # Remove /etc/volatile.cache if it exists in the target image
        rm -f ${D}${sysconfdir}/volatile.cache
    fi

    install -d ${D}${sysconfdir}/${BPN}/IPCAccessControl.d
    install \
        -t ${D}${sysconfdir}/${BPN}/IPCAccessControl.d \
        --mode 0600 \
        ${WORKDIR}/IPCAccessControl.d/*
}


# ==============================================================================
# PACKAGING
# ==============================================================================

CONFFILES:${PN} += "${sysconfdir}/${BPN}/IPCAccessControl.d/*"
