FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += " \
    file://0001-Merge-r1772414-from-trunk.patch \
"

PACKAGECONFIG_append = " ldap sqlite3 "
