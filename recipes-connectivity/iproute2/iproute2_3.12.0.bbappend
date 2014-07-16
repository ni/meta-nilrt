
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

PRINC := "${@int(PRINC) + 1}"

SRC_URI =+ "file://Add-align_kernel-macro.patch \
            file://fix-build-on-older-systems.patch"
