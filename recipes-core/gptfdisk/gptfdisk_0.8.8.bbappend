FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/files:"

DEPENDS = "popt util-linux"

SRC_URI =+ "file://0001-ni-remove-utf16-label-support.patch"
