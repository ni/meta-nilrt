SUMMARY = "Linux kernel NO_HZ_FULL polling test"
HOMEPAGE = "https://kernel.org"
SECTION = "tests"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://nohz_test.c;beginline=1;endline=2;md5=9e3e9401383732750e93a18064dc9493"

inherit ptest

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-files:"

S = "${WORKDIR}"

RDEPENDS_${PN}-ptest += "bash coreutils procps rt-tests"
RDEPENDS_${PN}-ptest_append_x64 += "packagegroup-ni-nohz-kernel"
ALLOW_EMPTY_${PN} = "1"

SRC_URI += "\
    file://run-ptest \
    file://nohz_test.c \
    file://PXIe-8880.conf \
    file://PXIe-8881.conf \
"

LDFLAGS += "-lpthread"

do_compile_ptest_append() {
    cd ${WORKDIR}
    ${CC} ${CFLAGS} -o nohz_test nohz_test.c ${LDFLAGS}
}

do_install_ptest_append() {
    install -m 0755 ${S}/run-ptest ${D}${PTEST_PATH}
    install -m 0755 ${S}/nohz_test ${D}${PTEST_PATH}
    install -m 0644 ${S}/PXIe-8880.conf ${D}${PTEST_PATH}
    install -m 0644 ${S}/PXIe-8881.conf ${D}${PTEST_PATH}
}

pkg_postinst_ontarget_${PN}-ptest_append() {
    CPUS=`nproc --all`
    ISOLATED_CPU=$((CPUS - 1))

    if [ $ISOLATED_CPU -gt 0 ]; then
        echo 'set otherbootargs="${otherbootargs} isolcpus='$ISOLATED_CPU' nohz_full='$ISOLATED_CPU' mitigations=off intel_pstate=disable intel_idle.max_cstate=0 processor.max_cstate=0 nosoftlockup mce=ignore_ce audit=0 tsc=nowatchdog"' > /boot/runmode/no-hz-full-params.cfg
        grep -qsxF 'source /runmode/no-hz-full-params.cfg' /boot/runmode/bootimage.cfg || echo 'source /runmode/no-hz-full-params.cfg' >> /boot/runmode/bootimage.cfg
    else
        echo "[kernel-test-nohz:error] This test requires a system with 2 or more CPUs"
        exit 1
    fi

    echo "[kernel-test-nohz:info] This test requires a reboot after install for kernel changes to take effect"
}

pkg_postrm_${PN}-ptest_append() {
    sed -i '/^source \/runmode\/no-hz-full-params\.cfg/d' /boot/runmode/bootimage.cfg
    rm -f /boot/runmode/no-hz-full-params.cfg
    echo "[kernel-test-nohz:info] This test requires a reboot after uninstall for kernel changes to take effect"
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
