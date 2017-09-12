require nilrt-u-boot.inc

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
LIC_FILES_CHKSUM_xilinx-zynqhf = "file://COPYING;md5=1707d6db1d42237583f50183a5651ecb"

SRC_URI = "\
        ${NILRT_GIT}/u-boot.git;protocol=git;branch=${UBOOT_BRANCH} \
"

SRC_URI_append_arm = "\
        file://fw_env-${MACHINE}.config \
"

SRC_URI_append_n310 = " \
	file://default-gcc.patch \
"

SRC_URI_append_xilinx-zynqhf = " \
        file://0001-Remove-hardcoded-softfp-from-arm-makefile.patch \
        file://fw-enw-fix-missing-stdint-h.patch \
        file://0001-gcc5-backport-add-compiler-gcc5.h.patch \
        file://0002-gcc5-use-gcc-inline-version-instead-c99.patch \
        file://0003-gcc5-include-io.h-needs-inline-def-from-compiler-gcc.h.patch \
	file://fix-build-error-under-gcc6.patch \
"

SRCREV = "${AUTOREV}"

do_compile_xilinx-zynqhf(){
    unset LDFLAGS
    unset CFLAGS
    unset CPPFLAGS
    oe_runmake ${UBOOT_MACHINE}
    oe_runmake HOSTCC="${CC}" HOSTSTRIP="${TARGET_PREFIX}strip" env
}

do_install_append(){
    install -m 0644 ${WORKDIR}/fw_env-${MACHINE}.config ${D}${sysconfdir}/fw_env.config
}
