FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI =+ " file://set_ulimit.patch"
