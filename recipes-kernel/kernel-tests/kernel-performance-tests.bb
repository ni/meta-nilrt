SUMMARY = "Linux kernel-specific performance tests"
HOMEPAGE = "https://kernel.org"
SECTION = "tests"
LICENSE = "GPL-2.0-only & GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-or-later;md5=fed54355545ffd980b814dab4a3b312c"

inherit ptest

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}-files:"

S = "${WORKDIR}"

DEPENDS = "virtual/kernel"
RDEPENDS:${PN}-ptest += "bash rt-tests fio iperf3 python3 python3-pip docker"
RDEPENDS:${PN}-ptest:append:x64 = " fw-printenv"
RDEPENDS:${PN}-ptest:append:armv7a = " u-boot-fw-utils"

ALLOW_EMPTY:${PN} = "1"

SRC_URI += "\
    file://run-ptest \
    file://run-cyclictest \
    file://upload_cyclictest_results.py \
    file://common.cfg \
    file://fio.cfg \
    file://test_kernel_cyclictest_idle.sh \
    file://test_kernel_cyclictest_hackbench.sh \
    file://test_kernel_cyclictest_fio.sh \
    file://test_kernel_cyclictest_iperf.sh \
    file://run-container-load \
    file://test_kernel_cyclictest_container_load.sh \
"

do_install_ptest:append() {
    install -m 0755 ${S}/run-ptest ${D}${PTEST_PATH}
    install -m 0755 ${S}/run-cyclictest ${D}${PTEST_PATH}
    install -m 0755 ${S}/upload_cyclictest_results.py ${D}${PTEST_PATH}
    install -m 0644 ${S}/common.cfg ${D}${PTEST_PATH}
    install -m 0644 ${S}/fio.cfg ${D}${PTEST_PATH}
    install -m 0755 ${S}/test_kernel_cyclictest_idle.sh ${D}${PTEST_PATH}
    install -m 0755 ${S}/test_kernel_cyclictest_hackbench.sh ${D}${PTEST_PATH}
    install -m 0755 ${S}/test_kernel_cyclictest_fio.sh ${D}${PTEST_PATH}
    install -m 0755 ${S}/test_kernel_cyclictest_iperf.sh ${D}${PTEST_PATH}
    install -m 0755 ${S}/run-container-load ${D}${PTEST_PATH}
    install -m 0755 ${S}/test_kernel_cyclictest_container_load.sh ${D}${PTEST_PATH}
}

pkg_postinst_ontarget:${PN}-ptest:append() {
    python3 -m pip install influxdb
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
