ENABLE_SRC_INSTALL_${PN} = "1"
PACKAGES += " ${PN}-src "

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
SRC_URI += " \
    file://0001-mod_ssl-Add-hooks-to-allow-other-modules-to-perform-.patch \
    file://0002-mod_ssl-Fixup-protocol-discovery-hooks-on-Windows-MS.patch \
"
