FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += " \
            file://0001-Implement-ficom-instruction-for-amd64.patch \
            file://0002-Patch-for-FBLD-FBSTP-FTST.patch \
           "

# Enable -src package creation, which is not done by default in nilrt
# OE recipes. valgrind-src is a runtime dependency of valgrind-ptest.

ENABLE_SRC_INSTALL = "1"
PACKAGE_DEBUG_SPLIT_STYLE = "debug-with-srcpkg"

