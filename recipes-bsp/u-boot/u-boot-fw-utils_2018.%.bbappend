require nilrt-u-boot.inc

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
LIC_FILES_CHKSUM = "file://COPYING;md5=1707d6db1d42237583f50183a5651ecb"

SRC_URI = "\
        ${NILRT_GIT}/u-boot.git;protocol=git;branch=${UBOOT_BRANCH} \
"

SRC_URI_append_arm = "\
        file://fw_env-${MACHINE}.config \
"

SRC_URI_append = " \
        file://fw-enw-fix-missing-stdint-h.patch \
        file://0003-gcc5-include-io.h-needs-inline-def-from-compiler-gcc.h.patch \
        file://0001-Makefile-Quote-the-HOSTCC-command-line-parameter.patch \
"

do_compile(){
    oe_runmake ${UBOOT_MACHINE}
    oe_runmake env

}

do_install_append(){
    install -d ${D}${sysconfdir}
    install -m 0644 ${WORKDIR}/fw_env-${MACHINE}.config ${D}${sysconfdir}/fw_env.config
    chown 0:${LVRT_GROUP} ${D}${base_sbindir}/fw_printenv
    chmod 4550 ${D}${base_sbindir}/fw_printenv
    ln -sf ${base_sbindir}/fw_printenv ${D}${base_sbindir}/fw_setenv
}
