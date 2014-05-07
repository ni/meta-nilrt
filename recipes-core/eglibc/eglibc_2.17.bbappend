
FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/files:${THISDIR}/${PN}-${PV}:"
PRINC := "${@int(PRINC) + 1}"

SRC_URI =+ "file://cp936_support.patch "
SRC_URI =+ "file://reduce_heapmaxsize.patch "
