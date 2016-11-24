FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += " \
	file://0001-main-add-new-AlwaysConnectedTechnologies-list-option.patch \
	file://0002-service-implement-AlwaysConnectedTechnologies-option.patch \
	file://0003-service-abstract-the-more-complex-autoconnect-condit.patch \
	file://0004-main.conf-document-AlwaysConnectedTechnologies-optio.patch \
	file://main.conf \
"

do_install_append() {
    install -d ${D}${sysconfdir}/connman
    install -m 0644 ${WORKDIR}/main.conf ${D}${sysconfdir}/connman/
}
