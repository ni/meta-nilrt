FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
PACKAGECONFIG_remove += "network json service"

RDEPENDS_${PN} += "dosfstools e2fsprogs"
