SUMMARY = "Linux kernel-specific performance tests"
HOMEPAGE = "https://kernel.org"
SECTION = "tests"
LICENSE = "GPLv2 & GPLv2+"
LIC_FILES_CHKSUM = "file://run-ptest;md5=905781abd96c0213c8b5bfa6c0681dd3"

inherit ptest

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-files:"

S = "${WORKDIR}"

DEPENDS = "virtual/kernel"
RDEPENDS_${PN}-ptest += "bash rt-tests fio iperf3"

ALLOW_EMPTY_${PN} = "1"

SRC_URI += "\
    file://run-ptest \
    file://run-cyclictest \
    file://common.cfg \
    file://fio.cfg \
    file://iperf.cfg \
    file://test_kernel_cyclictest_idle.sh \
    file://test_kernel_cyclictest_hackbench.sh \
    file://test_kernel_cyclictest_fio.sh \
    file://test_kernel_cyclictest_iperf.sh \
"

do_install_ptest_append() {
    install -m 0755 ${S}/run-ptest ${D}${PTEST_PATH}
    install -m 0755 ${S}/run-cyclictest ${D}${PTEST_PATH}
    install -m 0644 ${S}/common.cfg ${D}${PTEST_PATH}
    install -m 0644 ${S}/fio.cfg ${D}${PTEST_PATH}
    install -m 0644 ${S}/iperf.cfg ${D}${PTEST_PATH}
    install -m 0755 ${S}/test_kernel_cyclictest_idle.sh ${D}${PTEST_PATH}
    install -m 0755 ${S}/test_kernel_cyclictest_hackbench.sh ${D}${PTEST_PATH}
    install -m 0755 ${S}/test_kernel_cyclictest_fio.sh ${D}${PTEST_PATH}
    install -m 0755 ${S}/test_kernel_cyclictest_iperf.sh ${D}${PTEST_PATH}
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
