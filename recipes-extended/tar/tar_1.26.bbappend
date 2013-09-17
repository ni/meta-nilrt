
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

PRINC := "${@int(PRINC) + 1}"

SRC_URI =+ "file://tar-1.24-xattrs.patch"

DEPENDS_append = " acl"