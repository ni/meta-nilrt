SUMMARY = "Linux kernel NO_HZ_FULL polling test"
HOMEPAGE = "https://kernel.org"
SECTION = "tests"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://nohz_test.c;md5=d25cf78b2ec478f88868ccbc1967e9da"

inherit ptest

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-files:"

S = "${WORKDIR}"

RDEPENDS_${PN}-ptest += "bash procps rt-tests"
RDEPENDS_${PN}-ptest_append_x64 += "packagegroup-ni-nohz-kernel"
ALLOW_EMPTY_${PN} = "1"

SRC_URI += "\
    file://run-ptest \
    file://nohz_test.c \
"

LDFLAGS += "-lpthread"

do_compile_ptest_append() {
    cd ${WORKDIR}
    ${CC} ${CFLAGS} -o nohz_test nohz_test.c ${LDFLAGS}
}

do_install_ptest_append() {
    install -m 0755 ${S}/run-ptest ${D}${PTEST_PATH}
    install -m 0755 ${S}/nohz_test ${D}${PTEST_PATH}
}

pkg_postinst_ontarget_${PN}-ptest_append() {
    CPUS=`nproc`
    ISOLATED_CPU=$((CPUS - 1))
    OTHBOOTARGS=`fw_printenv othbootargs 2>/dev/null | sed -e "s/^othbootargs=//g" | sed -e "s/isolcpus=[^ ]*[ ]*\|nohz_full=[^ ]*[ ]*//g"`

    if [ $ISOLATED_CPU -gt 0 ]; then
        fw_setenv othbootargs $OTHBOOTARGS 'isolcpus='$ISOLATED_CPU' nohz_full='$ISOLATED_CPU
    else
        echo "[kernel-test-nohz:error] This test requires a system with 2 or more CPUs"
        exit 1
    fi

    echo "[kernel-test-nohz:info] This test requires a reboot after install for kernel changes to take effect"
}

pkg_postrm_${PN}-ptest_append() {
    OTHBOOTARGS=`fw_printenv othbootargs 2>/dev/null | sed -e "s/^othbootargs=//g" | sed -e "s/isolcpus=[^ ]*[ ]*\|nohz_full=[^ ]*[ ]*//g"`
    fw_setenv othbootargs $OTHBOOTARGS
    echo "[kernel-test-nohz:info] This test requires a reboot after uninstall for kernel changes to take effect"
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
