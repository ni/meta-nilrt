SUMMARY = "Linux kernel-specific containerized performance tests"
HOMEPAGE = "https://kernel.org"
SECTION = "tests"
LICENSE = "GPL-2.0-only & GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-or-later;md5=fed54355545ffd980b814dab4a3b312c"

inherit ptest

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}-files:"

S = "${WORKDIR}"

DEPENDS = "virtual/kernel"
RDEPENDS:${PN}-ptest += "bash python3 docker-ce"
RDEPENDS:${PN}-ptest:append:x64 = " fw-printenv"
RDEPENDS:${PN}-ptest:append:armv7a = " u-boot-fw-utils"

ALLOW_EMPTY:${PN} = "1"

SRC_URI += "\
    file://build-containers \
    file://run-ptest \
    file://run-cyclictest \
    file://upload_cyclictest_results.py \
    file://common.cfg \
    file://fio.cfg \
    file://fio-load \
    file://hackbench-load \
    file://iperf-load \
    file://test_kernel_cyclictest_idle_containerized.sh \
    file://test_kernel_cyclictest_hackbench_containerized.sh \
    file://test_kernel_cyclictest_fio_containerized.sh \
    file://test_kernel_cyclictest_iperf_containerized.sh \
    file://cyclictest-container/Dockerfile \
    file://parallel-container/Dockerfile \
"

do_install_ptest:append() {
    install -m 0755 ${S}/build-containers ${D}${PTEST_PATH}
    install -m 0755 ${S}/run-ptest ${D}${PTEST_PATH}
    install -m 0755 ${S}/run-cyclictest ${D}${PTEST_PATH}
    install -m 0755 ${S}/upload_cyclictest_results.py ${D}${PTEST_PATH}
    install -m 0644 ${S}/common.cfg ${D}${PTEST_PATH}
    install -m 0644 ${S}/fio.cfg ${D}${PTEST_PATH}
    install -m 0755 ${S}/fio-load ${D}${PTEST_PATH}
    install -m 0755 ${S}/hackbench-load ${D}${PTEST_PATH}
    install -m 0755 ${S}/iperf-load ${D}${PTEST_PATH}
    install -m 0755 ${S}/test_kernel_cyclictest_idle_containerized.sh ${D}${PTEST_PATH}
    install -m 0755 ${S}/test_kernel_cyclictest_hackbench_containerized.sh ${D}${PTEST_PATH}
    install -m 0755 ${S}/test_kernel_cyclictest_fio_containerized.sh ${D}${PTEST_PATH}
    install -m 0755 ${S}/test_kernel_cyclictest_iperf_containerized.sh ${D}${PTEST_PATH}

    mkdir -p ${D}${PTEST_PATH}/cyclictest-container
    install -m 0644 ${S}/cyclictest-container/Dockerfile ${D}${PTEST_PATH}/cyclictest-container

    mkdir -p ${D}${PTEST_PATH}/parallel-container
    install -m 0644 ${S}/parallel-container/Dockerfile ${D}${PTEST_PATH}/parallel-container
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
