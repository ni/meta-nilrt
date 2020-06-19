
FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/${PN}:"

SRC_URI =+ "file://cp936_support.patch \
            file://cp936-gconv-modules.patch \
           "
