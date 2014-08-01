
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI =+ "file://Add-align_kernel-macro.patch \
            file://fix-build-on-older-systems.patch"
