FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += " \
	file://main.conf \
"

do_install_append() {
    install -d ${D}${sysconfdir}/connman
    install -m 0644 ${WORKDIR}/main.conf ${D}${sysconfdir}/connman/
}

# transconf hook
inherit transconf-hook
SRC_URI =+ "file://transconf-hooks/"
RDEPENDS_${PN}-transconf += "bash"
TRANSCONF_HOOKS_${PN} = "transconf-hooks/connman"
