FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
LIC_FILES_CHKSUM = "file://COPYING;md5=1707d6db1d42237583f50183a5651ecb"
SRC_URI = "\
        git://git.amer.corp.natinst.com/u-boot.git;protocol=git;branch=nizynq/15.0/v2012.10 \
        file://fw_env-nizynq.config \
        "

SRC_URI_append_xilinx-zynqhf = "file://0001-Remove-hardcoded-softfp-from-arm-makefile.patch" 

SRCREV = "${AUTOREV}"

do_compile(){
    unset LDFLAGS
    unset CFLAGS
    unset CPPFLAGS
    oe_runmake ${UBOOT_MACHINE}
    oe_runmake HOSTCC="${CC}" HOSTSTRIP="${TARGET_PREFIX}strip" env
}

do_install(){
    install -d ${D}${base_sbindir}
    install -d ${D}${sysconfdir}
    install -m 755 ${S}/tools/env/fw_printenv ${D}${base_sbindir}/fw_printenv
    install -m 755 ${S}/tools/env/fw_printenv ${D}${base_sbindir}/fw_setenv
    install -m 0644 ${S}/../fw_env-nizynq.config ${D}${sysconfdir}/fw_env.config
}
