FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
PACKAGECONFIG_remove += "network json service"

RDEPENDS_${PN} += "dosfstools e2fsprogs"

SRC_URI += "file://0001-rauc-Add-command-line-option-to-skip-bundle-verifica.patch"
