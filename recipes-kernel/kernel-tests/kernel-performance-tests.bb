SUMMARY = "Linux kernel-specific performance tests"
HOMEPAGE = "https://kernel.org"
SECTION = "tests"
LICENSE = "GPLv2 & GPLv2+"
LIC_FILES_CHKSUM = "file://run-ptest;md5=e6b9dbe04e1c3de85402b8167a279f76"


DEPENDS = "virtual/kernel"


S = "${WORKDIR}"

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


inherit ptest
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}-files:"
ALLOW_EMPTY:${PN} = "1"


do_install_ptest:append() {
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
RDEPENDS:${PN}-ptest += "bash rt-tests fio iperf3"
RDEPENDS:${PN}-ptest:append_x64 += "fw-printenv"
RDEPENDS:${PN}-ptest:append_armv7a += "u-boot-fw-utils"
