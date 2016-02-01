
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI =+ "file://Add-align_kernel-macro.patch \
            file://0001-iproute2-fix-build-for-older-systems.patch"
